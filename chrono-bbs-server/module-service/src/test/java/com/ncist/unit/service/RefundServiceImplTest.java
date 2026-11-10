package com.ncist.unit.service;

import com.ncist.common.context.UserContext;
import com.ncist.common.enums.RefundErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.entity.OrderItem;
import com.ncist.entity.Product;
import com.ncist.entity.Refund;
import com.ncist.entity.RefundDetail;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderItemMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.RefundMapper;
import com.ncist.service.impl.RefundServiceImpl;
import com.ncist.vo.RefundVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RefundServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("退款服务单元测试")
class RefundServiceImplTest {

    @Mock
    private RefundMapper refundMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private MerchantMapper merchantMapper;

    @InjectMocks
    private RefundServiceImpl refundService;

    @BeforeEach
    void setUp() {
        UserContext.set(new UserContext.UserInfo(8L, "buyer", 1));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private OrderItem orderItem(long id, long orderId, long productId, int subtotal) {
        return OrderItem.builder()
                .id(id).orderId(orderId).productId(productId)
                .quantity(1).unitPrice(subtotal).subtotal(subtotal)
                .build();
    }

    private Order order(long id, long userId, int status) {
        return Order.builder().id(id).userId(userId).status(status).build();
    }

    private RefundDetail refundDetail(long id, long userId, long orderId, long orderItemId, long productId, int status) {
        RefundDetail d = new RefundDetail();
        d.setId(id);
        d.setOrderId(orderId);
        d.setOrderItemId(orderItemId);
        d.setUserId(userId);
        d.setType(1);
        d.setReason("质量问题");
        d.setAmount(5000);
        d.setStatus(status);
        d.setProductId(productId);
        d.setProductName("测试商品");
        return d;
    }

    private RefundCreateDTO createDTO(long orderItemId, Integer amount) {
        RefundCreateDTO dto = new RefundCreateDTO();
        dto.setOrderItemId(orderItemId);
        dto.setType(1);
        dto.setReason("质量问题");
        dto.setAmount(amount);
        return dto;
    }

    // ================ 申请退款 ================

    @Test
    @DisplayName("申请退款-已收货订单成功且订单进入退款中")
    void applySuccess() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));
        when(refundMapper.existsByOrderItemId(11L)).thenReturn(0);

        refundService.apply(8L, createDTO(11L, 5000));

        ArgumentCaptor<Refund> captor = ArgumentCaptor.forClass(Refund.class);
        verify(refundMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals(1L, captor.getValue().getOrderId());
        verify(orderMapper).markRefunding(1L);
    }

    @Test
    @DisplayName("申请退款-订单明细不存在返回404")
    void applyItemNotFound() {
        when(orderItemMapper.selectById(11L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.apply(8L, createDTO(11L, 5000)));
        assertEquals(RefundErrorCode.ORDER_ITEM_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("申请退款-非本人订单返回403")
    void applyNotOwner() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 9L, 4));

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.apply(8L, createDTO(11L, 5000)));
        assertEquals(RefundErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("申请退款-待支付订单不可退款返回400")
    void applyNotRefundable() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.apply(8L, createDTO(11L, 5000)));
        assertEquals(RefundErrorCode.ORDER_NOT_REFUNDABLE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("申请退款-重复申请返回409")
    void applyAlreadyRefunding() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));
        when(refundMapper.existsByOrderItemId(11L)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.apply(8L, createDTO(11L, 5000)));
        assertEquals(RefundErrorCode.ALREADY_REFUNDING.getCode(), e.getCode());
        verify(refundMapper, never()).insert(any(Refund.class));
    }

    @Test
    @DisplayName("申请退款-金额超过明细小计返回400")
    void applyAmountExceeded() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));
        when(refundMapper.existsByOrderItemId(11L)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.apply(8L, createDTO(11L, 5001)));
        assertEquals(RefundErrorCode.AMOUNT_INVALID.getCode(), e.getCode());
        verify(refundMapper, never()).insert(any(Refund.class));
    }

    // ================ 我的退款/详情 ================

    @Test
    @DisplayName("我的退款-分页组装")
    void myRefunds() {
        when(refundMapper.countByUser(8L)).thenReturn(1L);
        when(refundMapper.selectPageByUser(8L, 0, 10))
                .thenReturn(List.of(refundDetail(1L, 8L, 1L, 11L, 100L, 0)));

        PageResult<RefundVO> result = refundService.myRefunds(8L, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals("测试商品", result.getList().get(0).getProductName());
        assertEquals(0, result.getList().get(0).getStatus());
    }

    @Test
    @DisplayName("退款详情-成功")
    void detailSuccess() {
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));

        RefundVO vo = refundService.detail(8L, 1L);

        assertEquals(5000, vo.getAmount());
    }

    @Test
    @DisplayName("退款详情-不存在返回404")
    void detailNotFound() {
        when(refundMapper.selectDetailById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class, () -> refundService.detail(8L, 1L));
        assertEquals(RefundErrorCode.REFUND_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("退款详情-非本人返回403")
    void detailNotOwner() {
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 9L, 1L, 11L, 100L, 0));

        BusinessException e = assertThrows(BusinessException.class, () -> refundService.detail(8L, 1L));
        assertEquals(RefundErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    // ================ 审核 ================

    @Test
    @DisplayName("审核-管理员同意则订单变已退款")
    void reviewApproveByAdmin() {
        UserContext.set(new UserContext.UserInfo(1L, "admin", 3));
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        when(refundMapper.markReviewed(1L, 1, 1L, "同意")).thenReturn(1);

        refundService.review(1L, 1L, reviewDTO(1, "同意"));

        verify(orderMapper).markRefunded(1L);
    }

    @Test
    @DisplayName("审核-商家拒绝则订单恢复已收货")
    void reviewRejectByMerchant() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(1).build());
        when(productMapper.selectById(100L)).thenReturn(Product.builder().id(100L).merchantId(7L).build());
        when(refundMapper.markReviewed(1L, 2, 5L, "证据不足")).thenReturn(1);

        refundService.review(5L, 1L, reviewDTO(0, "证据不足"));

        verify(orderMapper).revertRefunding(1L);
    }

    @Test
    @DisplayName("审核-商家资质无效返回403")
    void reviewMerchantInvalid() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(2).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.review(5L, 1L, reviewDTO(1, "同意")));
        assertEquals(RefundErrorCode.NO_PERMISSION_REVIEW.getCode(), e.getCode());
    }

    @Test
    @DisplayName("审核-退款商品不归属该商家返回403")
    void reviewProductNotOwned() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(1).build());
        when(productMapper.selectById(100L)).thenReturn(Product.builder().id(100L).merchantId(88L).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.review(5L, 1L, reviewDTO(1, "同意")));
        assertEquals(RefundErrorCode.NO_PERMISSION_REVIEW.getCode(), e.getCode());
    }

    @Test
    @DisplayName("审核-普通用户返回403")
    void reviewByUserForbidden() {
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        when(merchantMapper.selectByUserId(8L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.review(8L, 1L, reviewDTO(1, "同意")));
        assertEquals(RefundErrorCode.NO_PERMISSION_REVIEW.getCode(), e.getCode());
    }

    @Test
    @DisplayName("审核-重复审核返回409")
    void reviewAlreadyReviewed() {
        when(refundMapper.selectDetailById(1L)).thenReturn(refundDetail(1L, 8L, 1L, 11L, 100L, 0));
        UserContext.set(new UserContext.UserInfo(1L, "admin", 3));
        when(refundMapper.markReviewed(1L, 1, 1L, "同意")).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.review(1L, 1L, reviewDTO(1, "同意")));
        assertEquals(RefundErrorCode.ALREADY_REVIEWED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("审核-退款单不存在返回404")
    void reviewNotFound() {
        when(refundMapper.selectDetailById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.review(1L, 1L, reviewDTO(1, "同意")));
        assertEquals(RefundErrorCode.REFUND_NOT_FOUND.getCode(), e.getCode());
    }

    // ================ 审核列表 ================

    @Test
    @DisplayName("商家退款列表-审核通过的商家仅查询自己的退款单")
    void merchantRefundsSuccess() {
        UserContext.set(new UserContext.UserInfo(9L, "merchant", 2));
        when(merchantMapper.selectByUserId(9L))
                .thenReturn(Merchant.builder().id(3L).userId(9L).status(1).build());
        when(refundMapper.countByMerchant(3L, 0)).thenReturn(1L);
        when(refundMapper.selectPageByMerchant(3L, 0, 0, 10))
                .thenReturn(List.of(refundDetail(1L, 8L, 1L, 11L, 100L, 0)));

        PageResult<RefundVO> result = refundService.merchantRefunds(9L, 0, 1, 10);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(0, result.getList().get(0).getStatus());
    }

    @Test
    @DisplayName("商家退款列表-未审核通过的商家返回403")
    void merchantRefundsNotApproved() {
        UserContext.set(new UserContext.UserInfo(9L, "merchant", 2));
        when(merchantMapper.selectByUserId(9L))
                .thenReturn(Merchant.builder().id(3L).userId(9L).status(0).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.merchantRefunds(9L, null, 1, 10));
        assertEquals(RefundErrorCode.NO_PERMISSION_REVIEW.getCode(), e.getCode());
    }

    @Test
    @DisplayName("管理员退款列表-查询全平台退款单")
    void adminRefundsSuccess() {
        UserContext.set(new UserContext.UserInfo(1L, "admin", 3));
        when(refundMapper.countAll(null)).thenReturn(2L);
        when(refundMapper.selectPageAll(null, 0, 10))
                .thenReturn(List.of(refundDetail(1L, 8L, 1L, 11L, 100L, 0),
                        refundDetail(2L, 9L, 2L, 12L, 101L, 2)));

        PageResult<RefundVO> result = refundService.adminRefunds(1L, null, 1, 10);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
    }

    @Test
    @DisplayName("管理员退款列表-非管理员返回403")
    void adminRefundsForbidden() {
        UserContext.set(new UserContext.UserInfo(9L, "merchant", 2));

        BusinessException e = assertThrows(BusinessException.class,
                () -> refundService.adminRefunds(9L, null, 1, 10));
        assertEquals(RefundErrorCode.NO_PERMISSION_REVIEW.getCode(), e.getCode());
    }

    private RefundReviewDTO reviewDTO(Integer approved, String reason) {
        RefundReviewDTO dto = new RefundReviewDTO();
        dto.setApproved(approved);
        dto.setReviewReason(reason);
        return dto;
    }
}
