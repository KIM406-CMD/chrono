package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.AddressErrorCode;
import com.ncist.common.enums.CartErrorCode;
import com.ncist.common.enums.OrderErrorCode;
import com.ncist.common.enums.SeckillErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.CreateOrderDTO;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.entity.Address;
import com.ncist.entity.CartItemDetail;
import com.ncist.entity.Order;
import com.ncist.entity.OrderItem;
import com.ncist.entity.OrderItemDetail;
import com.ncist.mapper.AddressMapper;
import com.ncist.mapper.CartItemMapper;
import com.ncist.mapper.OrderItemMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductSkuMapper;
import com.ncist.mapper.UserCouponMapper;
import com.ncist.service.OrderService;
import com.ncist.service.SeckillService;
import com.ncist.vo.CreateOrderVO;
import com.ncist.vo.OrderDetailVO;
import com.ncist.vo.OrderItemVO;
import com.ncist.vo.OrderListItemVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SubmitTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 *
 * <p>下单流程以 Redis 防重复提交 Token 保证幂等：Token 绑定用户、5 分钟有效，
 * 校验后通过删除操作的返回值确保只能消费一次。库存扣减使用
 * {@code UPDATE ... WHERE stock >= quantity} 原子完成，避免超卖。</p>
 *
 * <p>创建、取消与删除订单涉及多表写入，使用 {@code @Transactional} 保证一致性；
 * 取消订单会回补库存并回退优惠券。订单号为“时间戳 + 6 位随机数”，
 * 冲突时最多重试 5 次。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 防重复提交 Token 的 Redis key 前缀，值为绑定的用户 ID。 */
    private static final String SUBMIT_TOKEN_PREFIX = "order:submit-token:";
    /** 订单号中的时间部分格式：yyyyMMddHHmmss。 */
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /** 订单号随机后缀生成器（加密安全随机数）。 */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 订单数据访问组件。 */
    private final OrderMapper orderMapper;
    /** 订单明细数据访问组件。 */
    private final OrderItemMapper orderItemMapper;
    /** 购物车数据访问组件。 */
    private final CartItemMapper cartItemMapper;
    /** 收货地址数据访问组件。 */
    private final AddressMapper addressMapper;
    /** SKU 数据访问组件，用于原子扣减与回补库存。 */
    private final ProductSkuMapper productSkuMapper;
    /** 用户优惠券数据访问组件，用于核销与回退。 */
    private final UserCouponMapper userCouponMapper;
    /** 秒杀券服务，用于复用优惠计算逻辑。 */
    private final SeckillService seckillService;
    /** Redis 字符串操作模板，用于防重复提交 Token。 */
    private final StringRedisTemplate stringRedisTemplate;
    /** JSON 序列化组件，用于写入与解析地址快照。 */
    private final ObjectMapper objectMapper;

    /** {@inheritDoc} */
    @Override
    public SubmitTokenVO getSubmitToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = SUBMIT_TOKEN_PREFIX + token;
        // Token 绑定用户 ID，防止他人盗用
        stringRedisTemplate.opsForValue().set(key, String.valueOf(userId),
                java.time.Duration.ofMinutes(5));
        return SubmitTokenVO.builder().token(token).build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>流程：消费 Token → 校验地址属主 → 校验购物车项与库存 → 原子扣减库存 →
     * 计算优惠与订单金额 → 写入地址快照/订单/明细 → 清理购物车 → 核销优惠券。
     * 任一步骤失败均回滚事务。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVO createOrder(Long userId, CreateOrderDTO dto) {
        // 1. 校验并消费防重复提交 Token（校验绑定用户后，以 DELETE 影响 1 行为准，保证只可用一次）
        String tokenKey = SUBMIT_TOKEN_PREFIX + dto.getToken();
        String owner = stringRedisTemplate.opsForValue().get(tokenKey);
        if (owner == null || !owner.equals(String.valueOf(userId))) {
            throw new BusinessException(OrderErrorCode.SUBMIT_TOKEN_INVALID);
        }
        if (Boolean.FALSE.equals(stringRedisTemplate.delete(tokenKey))) {
            throw new BusinessException(OrderErrorCode.SUBMIT_TOKEN_INVALID);
        }
        // 2. 校验收货地址属主
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(AddressErrorCode.NO_PERMISSION_OPERATE);
        }
        // 3. 查询购物车选中项（SQL 限定属主），校验上架与库存
        List<Long> ids = dto.getCartItemIds().stream().distinct().toList();
        List<CartItemDetail> items = cartItemMapper.selectDetailByIds(userId, ids);
        if (items.isEmpty()) {
            throw new BusinessException(OrderErrorCode.CART_ITEMS_EMPTY);
        }
        for (CartItemDetail item : items) {
            if (item.getProductStatus() == null || item.getProductStatus() != 1) {
                throw new BusinessException(CartErrorCode.PRODUCT_OFF_SHELF);
            }
            if (item.getQuantity() > item.getStock()) {
                throw new BusinessException(CartErrorCode.STOCK_INSUFFICIENT);
            }
        }
        // 4. 原子锁库存（UPDATE ... WHERE stock >= 数量）
        for (CartItemDetail item : items) {
            if (productSkuMapper.deductStock(item.getSkuId(), item.getQuantity()) == 0) {
                throw new BusinessException(CartErrorCode.STOCK_INSUFFICIENT);
            }
        }
        // 5. 计算金额，用券抵扣（复用秒杀券 calculate 的校验与折扣逻辑）
        long totalAmount = items.stream()
                .mapToLong(item -> (long) item.getPrice() * item.getQuantity()).sum();
        SeckillCalculateVO calc = seckillService.calculate(
                SeckillCalculateDTO.builder().userCouponId(dto.getUserCouponId())
                        .totalAmount((int) totalAmount).build(),
                userId);
        // 6. 地址快照 + 订单号
        String snapshot = writeAddressSnapshot(address);
        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .addressSnapshot(snapshot)
                .totalAmount(calc.getFinalAmount())
                .status(1)
                .remark(dto.getRemark())
                .build();
        orderMapper.insert(order);
        // 7. 明细写入下单时价格快照
        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(order.getId())
                        .productId(item.getProductId())
                        .skuId(item.getSkuId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPrice())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .build())
                .toList();
        orderItemMapper.insertBatch(orderItems);
        // 8. 清理已下单的购物车项
        cartItemMapper.deleteByIds(userId, ids);
        // 9. 核销优惠券（状态条件保证并发下不重复核销）
        if (dto.getUserCouponId() != null
                && userCouponMapper.markUsed(dto.getUserCouponId(), order.getId()) == 0) {
            throw new BusinessException(SeckillErrorCode.COUPON_USED_OR_EXPIRED);
        }
        return CreateOrderVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>批量查询订单项后按订单分组，组装订单项数量与首项名称/图片，避免 N+1 查询。</p>
     */
    @Override
    public PageResult<OrderListItemVO> listOrders(Long userId, Integer status, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = orderMapper.countByUser(userId, status);
        List<Order> orders = total == 0 ? List.of()
                : orderMapper.selectPage(userId, status, (p - 1) * s, s);
        if (orders.isEmpty()) {
            return PageResult.of(0L, p, s, List.of());
        }
        // 批量联查明细，组装 itemCount/首项信息
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItemDetail>> itemsByOrder = orderItemMapper.selectDetailByOrderIds(orderIds)
                .stream().collect(Collectors.groupingBy(OrderItemDetail::getOrderId));
        List<OrderListItemVO> list = orders.stream()
                .map(order -> toListItem(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /** {@inheritDoc} */
    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long id) {
        Order order = requireOwnedOrder(id, userId);
        List<OrderItemVO> items = orderItemMapper.selectDetailByOrderId(id).stream()
                .map(this::toItemVO)
                .toList();
        OrderDetailVO.OrderDetailVOBuilder builder = OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .addressSnapshot(order.getAddressSnapshot())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .remark(order.getRemark())
                .cancelReason(order.getCancelReason())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .receivedAt(order.getReceivedAt())
                .completedAt(order.getCompletedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items);
        return builder.build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>先以状态条件更新取消订单，成功后逐项回补库存并把优惠券恢复为未使用；
     * 整个过程在事务中完成。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long id, String reason) {
        requireOwnedOrder(id, userId);
        // 状态条件更新，0 行即状态不允许
        if (orderMapper.cancel(id, userId, reason) == 0) {
            throw new BusinessException(OrderErrorCode.STATUS_ILLEGAL);
        }
        // 回补库存
        for (OrderItem item : orderItemMapper.selectByOrderId(id)) {
            productSkuMapper.restoreStock(item.getSkuId(), item.getQuantity());
        }
        // 回退优惠券为未使用
        userCouponMapper.revokeByOrderId(id);
    }

    /** {@inheritDoc} */
    @Override
    public void receiveOrder(Long userId, Long id) {
        requireOwnedOrder(id, userId);
        if (orderMapper.markReceived(id, userId) == 0) {
            throw new BusinessException(OrderErrorCode.STATUS_ILLEGAL);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>先按状态条件删除订单，成功后删除对应订单明细，事务保证两者一致。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long userId, Long id) {
        requireOwnedOrder(id, userId);
        if (orderMapper.deleteById(id, userId) == 0) {
            throw new BusinessException(OrderErrorCode.STATUS_ILLEGAL);
        }
        orderItemMapper.deleteByOrderId(id);
    }

    // ================ 私有校验与组装 ================

    /**
     * 查询订单并校验属主。
     *
     * @param id     订单 ID
     * @param userId 当前用户 ID
     * @return 订单实体
     * @throws BusinessException 订单不存在或不属于当前用户时抛出
     */
    private Order requireOwnedOrder(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.NO_PERMISSION_OPERATE);
        }
        return order;
    }

    /**
     * 将订单及其明细组装为列表项视图对象。
     *
     * @param order 订单实体
     * @param items 该订单的明细
     * @return 订单列表项，包含明细数量与首项名称/图片
     */
    private OrderListItemVO toListItem(Order order, List<OrderItemDetail> items) {
        OrderItemDetail first = items.stream()
                .min(java.util.Comparator.comparing(OrderItemDetail::getId)).orElse(null);
        return OrderListItemVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .itemCount(items.size())
                .firstItemName(first == null ? null : first.getProductName())
                .firstItemImage(first == null ? null : firstImage(first.getProductImages()))
                .build();
    }

    /**
     * 将订单明细联查结果转换为订单项视图对象。
     *
     * @param detail 订单明细联查结果
     * @return 订单项视图对象
     */
    private OrderItemVO toItemVO(OrderItemDetail detail) {
        return OrderItemVO.builder()
                .id(detail.getId())
                .orderId(detail.getOrderId())
                .productId(detail.getProductId())
                .skuId(detail.getSkuId())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getSubtotal())
                .productName(detail.getProductName())
                .productImage(firstImage(detail.getProductImages()))
                .skuSpecText(detail.getSpecText())
                .build();
    }

    /**
     * 将收货地址序列化为订单地址快照。
     *
     * @param address 收货地址
     * @return 地址快照 JSON；序列化失败时返回 null 并记录告警
     */
    private String writeAddressSnapshot(Address address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (Exception e) {
            log.warn("地址快照序列化失败: {}", address.getId());
            return null;
        }
    }

    /**
     * 解析商品图片 JSON 数组并返回首张图片。
     *
     * @param images 商品图片 JSON 数组
     * @return 首张图片 URL；数据为空或解析失败时返回 null
     */
    private String firstImage(String images) {
        if (images == null || images.isBlank()) {
            return null;
        }
        try {
            List<String> list = objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.warn("商品图片 JSON 解析失败: {}", images);
            return null;
        }
    }

    /**
     * 生成订单号：yyyyMMddHHmmss + 6 位随机数，数据库冲突时最多重试 5 次。
     *
     * @return 未被占用的订单号
     * @throws BusinessException 连续 5 次生成的订单号均已存在时抛出
     */
    private String generateOrderNo() {
        for (int i = 0; i < 5; i++) {
            String orderNo = LocalDateTime.now().format(ORDER_NO_FORMAT)
                    + String.format("%06d", RANDOM.nextInt(1000000));
            if (orderMapper.selectByOrderNo(orderNo) == null) {
                return orderNo;
            }
        }
        throw new BusinessException(OrderErrorCode.ORDER_NO_GENERATE_FAILED);
    }
}
