package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.MerchantErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.entity.Address;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.service.MerchantService;
import com.ncist.vo.MerchantInfoVO;
import com.ncist.vo.MerchantOrderVO;
import com.ncist.vo.MerchantStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家管理服务实现。
 *
 * <p>入驻申请与发货使用 {@code @Transactional} 保证写入一致性。
 * 查询与管理操作均要求用户存在商家记录，其中商品、订单、发货与统计
 * 还要求商家审核通过（status=1），且只能访问归属自己的数据。</p>
 *
 * <p>商家订单列表采用“先分页查询订单 ID，再批量加载订单行与商品明细”的方式，
 * 避免逐单查询造成的 N+1 问题；地址快照与商品图片 JSON 解析失败时仅降级为空值。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    /** 商家数据访问组件。 */
    private final MerchantMapper merchantMapper;
    /** 订单数据访问组件，用于发货状态校验与更新。 */
    private final OrderMapper orderMapper;
    /** JSON 序列化组件，用于解析地址快照与商品图片。 */
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     *
     * <p>申请记录初始状态写为 0（待审核）；事务保证单次申请写入的原子性。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyDTO dto) {
        // 同一用户只能申请一次
        Merchant existing = merchantMapper.selectByUserId(userId);
        if (existing != null) {
            throw new BusinessException(MerchantErrorCode.ALREADY_MERCHANT);
        }
        Merchant merchant = Merchant.builder()
                .userId(userId)
                .shopName(dto.getShopName())
                .businessLicense(dto.getBusinessLicense())
                .contactPhone(dto.getContactPhone())
                .status(0) // 待审核
                .build();
        merchantMapper.insertApply(merchant);
    }

    /** {@inheritDoc} */
    @Override
    public MerchantInfoVO getInfo(Long userId) {
        Merchant merchant = requireMerchant(userId);
        return MerchantInfoVO.builder()
                .id(merchant.getId())
                .userId(merchant.getUserId())
                .shopName(merchant.getShopName())
                .businessLicense(merchant.getBusinessLicense())
                .contactPhone(merchant.getContactPhone())
                .status(merchant.getStatus())
                .reviewReason(merchant.getReviewReason())
                .reviewedAt(merchant.getReviewedAt())
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<Map<String, Object>> listProducts(Long userId, Integer status, Integer page, Integer size) {
        Merchant merchant = requireActiveMerchant(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = merchantMapper.countMerchantProducts(merchant.getId(), status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Map<String, Object>> list = merchantMapper.selectMerchantProducts(
                merchant.getId(), status, (p - 1) * s, s);
        return PageResult.of(total, p, s, list);
    }

    /**
     * {@inheritDoc}
     *
     * <p>先分页查询订单 ID，再批量查询订单行与每个订单的商品明细，
     * 在内存中组装收件人信息与商品快照。</p>
     */
    @Override
    public PageResult<MerchantOrderVO> listOrders(Long userId, Integer status, Integer page, Integer size) {
        Merchant merchant = requireActiveMerchant(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = merchantMapper.countMerchantOrders(merchant.getId(), status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Long> orderIds = merchantMapper.selectMerchantOrderIds(
                merchant.getId(), status, (p - 1) * s, s);
        List<Map<String, Object>> orderRows = merchantMapper.selectMerchantOrders(
                merchant.getId(), orderIds);
        // 批量加载每个订单的商品明细
        Map<Long, List<Map<String, Object>>> itemsByOrder = orderIds.stream()
                .collect(Collectors.toMap(
                        oid -> oid,
                        oid -> merchantMapper.selectMerchantOrderItems(merchant.getId(), oid)));
        List<MerchantOrderVO> list = orderRows.stream()
                .map(row -> toMerchantOrder(row, itemsByOrder.get(toLong(row.get("id")))))
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /**
     * {@inheritDoc}
     *
     * <p>先校验订单所有商品都归属当前商家，再执行“已支付 → 已发货”的条件更新；
     * 更新影响 0 行说明状态已变化，事务回滚并返回业务异常。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long userId, Long orderId) {
        Merchant merchant = requireActiveMerchant(userId);
        // 1. 校验订单存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(MerchantErrorCode.ORDER_NOT_BELONG_TO_YOU);
        }
        // 2. 校验订单商品全部属于当前商家
        int notBelongCount = merchantMapper.countOrderItemsNotBelongTo(orderId, merchant.getId());
        if (notBelongCount > 0) {
            throw new BusinessException(MerchantErrorCode.ORDER_NOT_BELONG_TO_YOU);
        }
        // 3. 状态条件更新：已支付 → 已发货
        if (orderMapper.markShipped(orderId) == 0) {
            throw new BusinessException(MerchantErrorCode.SHIPPED_STATUS_ILLEGAL);
        }
    }

    /** {@inheritDoc} */
    @Override
    public MerchantStatsVO getStats(Long userId, String startDate, String endDate) {
        Merchant merchant = requireActiveMerchant(userId);
        Long totalSales = merchantMapper.sumSales(merchant.getId(), startDate, endDate);
        if (totalSales == null) {
            totalSales = 0L;
        }
        Long orderCount = merchantMapper.countOrders(merchant.getId(), startDate, endDate);
        if (orderCount == null) {
            orderCount = 0L;
        }
        List<MerchantStatsVO.ProductSales> topProducts = merchantMapper
                .selectTopProducts(merchant.getId(), startDate, endDate, 10).stream()
                .map(row -> MerchantStatsVO.ProductSales.builder()
                        .productId(toLong(row.get("productId")))
                        .productName((String) row.get("productName"))
                        .salesCount(toLong(row.get("salesCount")))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<MerchantStatsVO.CategorySales> salesByCategory = merchantMapper
                .selectSalesByCategory(merchant.getId(), startDate, endDate).stream()
                .map(row -> MerchantStatsVO.CategorySales.builder()
                        .categoryId(toLong(row.get("categoryId")))
                        .categoryName((String) row.get("categoryName"))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<MerchantStatsVO.TrendPoint> salesTrend = merchantMapper
                .selectSalesTrend(merchant.getId(), startDate, endDate).stream()
                .map(row -> MerchantStatsVO.TrendPoint.builder()
                        .date(String.valueOf(row.get("date")))
                        .amount(toLong(row.get("amount")))
                        .build())
                .toList();
        return MerchantStatsVO.builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .topProducts(topProducts)
                .salesByCategory(salesByCategory)
                .salesTrend(salesTrend)
                .build();
    }

    // ================ 私有辅助 ================

    /**
     * 查询并校验当前用户是已审核通过（status=1）的商家。
     *
     * @param userId 用户 ID
     * @return 商家实体
     * @throws BusinessException 用户不存在商家记录或商家状态不是已通过时抛出
     */
    private Merchant requireActiveMerchant(Long userId) {
        Merchant m = merchantMapper.selectByUserId(userId);
        if (m == null) {
            throw new BusinessException(MerchantErrorCode.NOT_MERCHANT);
        }
        if (m.getStatus() == null || m.getStatus() != 1) {
            throw new BusinessException(MerchantErrorCode.MERCHANT_STATUS_ILLEGAL);
        }
        return m;
    }

    /**
     * 查询当前用户的商家记录，不限制审核状态（用于获取商家信息）。
     *
     * @param userId 用户 ID
     * @return 商家实体
     * @throws BusinessException 用户不存在商家记录时抛出
     */
    private Merchant requireMerchant(Long userId) {
        Merchant m = merchantMapper.selectByUserId(userId);
        if (m == null) {
            throw new BusinessException(MerchantErrorCode.NOT_MERCHANT);
        }
        return m;
    }

    /**
     * 从 Map 查询结果中取出 Long 值，兼容 Integer/Long/BigDecimal 等数值类型。
     *
     * @param value 原始值
     * @return 转换后的 Long；value 为 null 时返回 0
     */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 从 Map 查询结果中取出 Integer 值，兼容 Integer/Long/BigDecimal 等数值类型。
     *
     * @param value 原始值
     * @return 转换后的 Integer；value 为 null 时返回 0
     */
    private Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number num) {
            return num.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    /**
     * 将订单行与商品明细组装为商家订单视图对象。
     *
     * @param row   订单行数据
     * @param items 属于当前商家的订单项数据
     * @return 商家订单视图对象
     */
    private MerchantOrderVO toMerchantOrder(Map<String, Object> row, List<Map<String, Object>> items) {
        MerchantOrderVO.MerchantOrderVOBuilder builder = MerchantOrderVO.builder()
                .id(toLong(row.get("id")))
                .orderNo((String) row.get("orderNo"))
                .userId(toLong(row.get("userId")))
                .totalAmount(toInt(row.get("totalAmount")))
                .status(toInt(row.get("status")))
                .paidAt(toLocalDateTime(row.get("paidAt")))
                .shippedAt(toLocalDateTime(row.get("shippedAt")))
                .createdAt(toLocalDateTime(row.get("createdAt")))
                .itemCount(toInt(row.get("itemCount")));
        // 解析 addressSnapshot
        Address addr = parseAddress((String) row.get("addressSnapshot"));
        if (addr != null) {
            builder.receiverName(addr.getReceiverName())
                    .receiverPhone(addr.getPhone())
                    .receiverAddress(buildAddress(addr));
        }
        // 组装该商家商品明细
        if (items != null && !items.isEmpty()) {
            builder.items(items.stream()
                    .map(it -> MerchantOrderVO.ItemSnapshot.builder()
                            .productId(toLong(it.get("productId")))
                            .productName((String) it.get("productName"))
                            .productImage(firstImage((String) it.get("productImages")))
                            .quantity(toInt(it.get("quantity")))
                            .unitPrice(toInt(it.get("unitPrice")))
                            .subtotal(toInt(it.get("subtotal")))
                            .build())
                    .toList());
        } else {
            builder.items(List.of());
        }
        return builder.build();
    }

    /**
     * 解析订单中保存的地址快照 JSON。
     *
     * @param snapshot 地址快照 JSON 字符串
     * @return 地址实体；为空或解析失败时返回 null
     */
    private Address parseAddress(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot, Address.class);
        } catch (Exception e) {
            log.warn("地址快照解析失败: {}", snapshot);
            return null;
        }
    }

    /**
     * 拼接省、市、区与详细地址，生成展示用完整地址。
     *
     * @param addr 地址实体
     * @return 完整地址字符串；全部字段为空时返回 null
     */
    private String buildAddress(Address addr) {
        StringBuilder sb = new StringBuilder();
        if (addr.getProvince() != null) sb.append(addr.getProvince());
        if (addr.getCity() != null) sb.append(addr.getCity());
        if (addr.getDistrict() != null) sb.append(addr.getDistrict());
        if (addr.getDetail() != null) sb.append(" ").append(addr.getDetail());
        return sb.toString().isBlank() ? null : sb.toString();
    }

    /**
     * 解析商品图片 JSON 数组并返回首张图片。
     *
     * @param imagesJson 商品图片 JSON 数组
     * @return 首张图片 URL；数据为空或解析失败时返回 null
     */
    private String firstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return null;
        }
        try {
            List<String> list = objectMapper.readValue(imagesJson, new TypeReference<>() {});
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.warn("商品图片 JSON 解析失败: {}", imagesJson);
            return null;
        }
    }

    /**
     * 将 Map 结果中的时间值转换为 {@link java.time.LocalDateTime}。
     *
     * @param value 原始时间值，可能为 LocalDateTime、Timestamp 或可解析字符串
     * @return 转换后的时间；value 为空或解析失败时返回 null
     */
    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.time.LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        try {
            return java.time.LocalDateTime.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
