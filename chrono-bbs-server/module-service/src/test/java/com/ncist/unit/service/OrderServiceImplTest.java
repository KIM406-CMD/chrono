package com.ncist.unit.service;

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
import com.ncist.service.SeckillService;
import com.ncist.service.impl.OrderServiceImpl;
import com.ncist.vo.CreateOrderVO;
import com.ncist.vo.OrderDetailVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SubmitTokenVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceImplTest {

    private static final Long USER_ID = 1001L;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private SeckillService seckillService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderDTO dto(Long addressId, List<Long> cartItemIds, Long userCouponId) {
        return CreateOrderDTO.builder()
                .token("tok-123")
                .addressId(addressId)
                .cartItemIds(cartItemIds)
                .remark("尽快发货")
                .userCouponId(userCouponId)
                .build();
    }

    private Address address(Long id) {
        return Address.builder()
                .id(id).userId(USER_ID).receiverName("张三").phone("13800138000")
                .province("河南省").city("新乡市").district("红旗区").detail("XX路1号").isDefault(1)
                .build();
    }

    private CartItemDetail cartItem(long id, long skuId, int quantity, int price, int stock, int productStatus) {
        CartItemDetail item = new CartItemDetail();
        item.setId(id);
        item.setUserId(USER_ID);
        item.setProductId(100L);
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setStock(stock);
        item.setProductStatus(productStatus);
        return item;
    }

    private SeckillCalculateVO calc(Integer finalAmount) {
        return SeckillCalculateVO.builder()
                .originalAmount(1199800).discountAmount(100000).finalAmount(finalAmount).couponName("满减券")
                .build();
    }

    private void stubTokenValid() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get("order:submit-token:tok-123")).thenReturn(String.valueOf(USER_ID));
        lenient().when(stringRedisTemplate.delete("order:submit-token:tok-123")).thenReturn(true);
    }

    private void stubHappyPath() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(address(5L));
        when(cartItemMapper.selectDetailByIds(eq(USER_ID), anyList()))
                .thenReturn(List.of(cartItem(1L, 10L, 2, 599900, 99, 1)));
        when(productSkuMapper.deductStock(10L, 2)).thenReturn(1);
        when(orderMapper.selectByOrderNo(anyString())).thenReturn(null);
        // 模拟 useGeneratedKeys 回填自增主键
        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(77L);
            return 1;
        }).when(orderMapper).insert(any(Order.class));
    }

    // ================ 防重复提交 Token ================

    @Test
    @DisplayName("获取下单 Token-写入 Redis 并绑定用户 5 分钟有效")
    void getSubmitTokenStoresBoundToken() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        SubmitTokenVO vo = orderService.getSubmitToken(USER_ID);

        assertNotNull(vo.getToken());
        verify(valueOperations).set(contains("order:submit-token:"), eq(String.valueOf(USER_ID)),
                any(Duration.class));
    }

    // ================ 创建订单 ================

    @Test
    @DisplayName("创建订单-Token 无效返回 400")
    void createTokenInvalid() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(OrderErrorCode.SUBMIT_TOKEN_INVALID.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-地址不存在返回 404")
    void createAddressNotFound() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(AddressErrorCode.ADDRESS_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-操作他人地址返回 403")
    void createAddressNotOwner() {
        stubTokenValid();
        Address other = address(5L);
        other.setUserId(9999L);
        when(addressMapper.selectById(5L)).thenReturn(other);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(AddressErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-购物车项为空返回 400")
    void createCartItemsEmpty() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(address(5L));
        when(cartItemMapper.selectDetailByIds(eq(USER_ID), anyList())).thenReturn(List.of());

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(OrderErrorCode.CART_ITEMS_EMPTY.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-商品下架返回 400")
    void createProductOffShelf() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(address(5L));
        when(cartItemMapper.selectDetailByIds(eq(USER_ID), anyList()))
                .thenReturn(List.of(cartItem(1L, 10L, 1, 599900, 99, 0)));

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(CartErrorCode.PRODUCT_OFF_SHELF.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-预校验库存不足返回 400")
    void createStockInsufficient() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(address(5L));
        when(cartItemMapper.selectDetailByIds(eq(USER_ID), anyList()))
                .thenReturn(List.of(cartItem(1L, 10L, 100, 599900, 99, 1)));

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(CartErrorCode.STOCK_INSUFFICIENT.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-锁库存失败返回 400")
    void createDeductStockFailed() {
        stubTokenValid();
        when(addressMapper.selectById(5L)).thenReturn(address(5L));
        when(cartItemMapper.selectDetailByIds(eq(USER_ID), anyList()))
                .thenReturn(List.of(cartItem(1L, 10L, 2, 599900, 99, 1)));
        when(productSkuMapper.deductStock(10L, 2)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), null)));

        assertEquals(CartErrorCode.STOCK_INSUFFICIENT.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建订单-成功不用券：金额为原价，写明细并清理购物车")
    void createSuccessWithoutCoupon() {
        stubHappyPath();
        when(seckillService.calculate(any(SeckillCalculateDTO.class), eq(USER_ID))).thenReturn(calc(1199800));

        CreateOrderVO vo = orderService.createOrder(USER_ID, dto(5L, List.of(1L, 1L), null));

        assertEquals(1199800, vo.getTotalAmount());
        assertEquals(1, vo.getStatus());
        assertNotNull(vo.getOrderNo());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(orderCaptor.capture());
        assertEquals("尽快发货", orderCaptor.getValue().getRemark());
        assertNotNull(orderCaptor.getValue().getAddressSnapshot());
        verify(orderItemMapper).insertBatch(anyList());
        verify(cartItemMapper).deleteByIds(eq(USER_ID), anyList());
        verify(userCouponMapper, never()).markUsed(anyLong(), anyLong());
    }

    @Test
    @DisplayName("创建订单-用券成功：实付为折后金额并核销优惠券")
    void createSuccessWithCoupon() {
        stubHappyPath();
        when(seckillService.calculate(any(SeckillCalculateDTO.class), eq(USER_ID))).thenReturn(calc(1099800));
        when(userCouponMapper.markUsed(eq(6L), anyLong())).thenReturn(1);

        CreateOrderVO vo = orderService.createOrder(USER_ID, dto(5L, List.of(1L), 6L));

        assertEquals(1099800, vo.getTotalAmount());
        verify(userCouponMapper).markUsed(eq(6L), anyLong());
    }

    @Test
    @DisplayName("创建订单-券核销竞争失败返回 409")
    void createCouponMarkUsedRace() {
        stubHappyPath();
        when(seckillService.calculate(any(SeckillCalculateDTO.class), eq(USER_ID))).thenReturn(calc(1099800));
        when(userCouponMapper.markUsed(eq(6L), anyLong())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.createOrder(USER_ID, dto(5L, List.of(1L), 6L)));

        assertEquals(SeckillErrorCode.COUPON_USED_OR_EXPIRED.getCode(), e.getCode());
    }

    // ================ 订单列表与详情 ================

    private Order order(long id, int status) {
        return Order.builder()
                .id(id).orderNo("202410221530001234").userId(USER_ID)
                .totalAmount(1199800).status(status).createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private OrderItemDetail itemDetail(long id, long orderId, String name) {
        OrderItemDetail d = new OrderItemDetail();
        d.setId(id);
        d.setOrderId(orderId);
        d.setProductId(100L);
        d.setSkuId(10L);
        d.setQuantity(2);
        d.setUnitPrice(599900);
        d.setSubtotal(1199800);
        d.setProductName(name);
        d.setProductImages("[\"https://img.example.com/p1.jpg\",\"https://img.example.com/p2.jpg\"]");
        d.setSpecText("红色 / 64GB");
        return d;
    }

    @Test
    @DisplayName("订单列表-空结果返回空分页")
    void listEmpty() {
        when(orderMapper.countByUser(USER_ID, null)).thenReturn(0L);

        PageResult<com.ncist.vo.OrderListItemVO> result = orderService.listOrders(USER_ID, null, 1, 10);

        assertEquals(0, result.getList().size());
        verify(orderMapper, never()).selectPage(anyLong(), any(), eq(0), eq(10));
    }

    @Test
    @DisplayName("订单列表-组装条目数与首项信息")
    void listAssemblesItems() {
        when(orderMapper.countByUser(USER_ID, null)).thenReturn(1L);
        when(orderMapper.selectPage(eq(USER_ID), eq(null), eq(0), eq(10))).thenReturn(List.of(order(1L, 1)));
        when(orderItemMapper.selectDetailByOrderIds(anyList()))
                .thenReturn(List.of(itemDetail(2L, 1L, "iPhone 15"), itemDetail(1L, 1L, "iPad")));

        PageResult<com.ncist.vo.OrderListItemVO> result = orderService.listOrders(USER_ID, null, null, null);

        assertEquals(1, result.getList().size());
        com.ncist.vo.OrderListItemVO item = result.getList().get(0);
        assertEquals(2, item.getItemCount());
        assertEquals("iPad", item.getFirstItemName());
        assertEquals("https://img.example.com/p1.jpg", item.getFirstItemImage());
    }

    @Test
    @DisplayName("订单详情-不存在返回 404")
    void detailNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.getOrderDetail(USER_ID, 1L));

        assertEquals(OrderErrorCode.ORDER_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("订单详情-越权返回 403")
    void detailNotOwner() {
        Order other = order(1L, 1);
        other.setUserId(9999L);
        when(orderMapper.selectById(1L)).thenReturn(other);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.getOrderDetail(USER_ID, 1L));

        assertEquals(OrderErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("订单详情-成功返回订单与明细")
    void detailSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 1));
        when(orderItemMapper.selectDetailByOrderId(1L)).thenReturn(List.of(itemDetail(1L, 1L, "iPhone 15")));

        OrderDetailVO vo = orderService.getOrderDetail(USER_ID, 1L);

        assertEquals(1, vo.getItems().size());
        assertEquals("iPhone 15", vo.getItems().get(0).getProductName());
        assertEquals("红色 / 64GB", vo.getItems().get(0).getSkuSpecText());
        assertEquals("https://img.example.com/p1.jpg", vo.getItems().get(0).getProductImage());
    }

    // ================ 取消/收货/删除 ================

    @Test
    @DisplayName("取消订单-仅待支付可取消并回补库存回退券")
    void cancelSuccessRestoresStockAndCoupon() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 1));
        when(orderMapper.cancel(eq(1L), eq(USER_ID), eq("不想要了"))).thenReturn(1);
        OrderItem orderItem = OrderItem.builder().id(1L).orderId(1L).productId(100L)
                .skuId(10L).quantity(2).unitPrice(599900).subtotal(1199800).build();
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(List.of(orderItem));

        orderService.cancelOrder(USER_ID, 1L, "不想要了");

        verify(productSkuMapper).restoreStock(10L, 2);
        verify(userCouponMapper).revokeByOrderId(1L);
    }

    @Test
    @DisplayName("取消订单-非待支付状态返回 400")
    void cancelStatusIllegal() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 2));
        when(orderMapper.cancel(eq(1L), eq(USER_ID), any())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(USER_ID, 1L, "不想要了"));

        assertEquals(OrderErrorCode.STATUS_ILLEGAL.getCode(), e.getCode());
        verify(productSkuMapper, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("确认收货-仅已发货状态可确认")
    void receiveStatusIllegal() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 2));
        when(orderMapper.markReceived(1L, USER_ID)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.receiveOrder(USER_ID, 1L));

        assertEquals(OrderErrorCode.STATUS_ILLEGAL.getCode(), e.getCode());
    }

    @Test
    @DisplayName("确认收货-成功")
    void receiveSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 3));
        when(orderMapper.markReceived(1L, USER_ID)).thenReturn(1);

        orderService.receiveOrder(USER_ID, 1L);

        verify(orderMapper).markReceived(1L, USER_ID);
    }

    @Test
    @DisplayName("删除订单-非已完成/已取消返回 400")
    void deleteStatusIllegal() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 1));
        when(orderMapper.deleteById(1L, USER_ID)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.deleteOrder(USER_ID, 1L));

        assertEquals(OrderErrorCode.STATUS_ILLEGAL.getCode(), e.getCode());
        verify(orderItemMapper, never()).deleteByOrderId(anyLong());
    }

    @Test
    @DisplayName("删除订单-成功同时删除明细")
    void deleteSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 5));
        when(orderMapper.deleteById(1L, USER_ID)).thenReturn(1);

        orderService.deleteOrder(USER_ID, 1L);

        verify(orderItemMapper).deleteByOrderId(1L);
    }
}
