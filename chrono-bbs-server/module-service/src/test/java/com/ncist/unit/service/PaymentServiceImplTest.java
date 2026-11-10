package com.ncist.unit.service;

import com.ncist.common.enums.OrderErrorCode;
import com.ncist.common.enums.PaymentErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.CardPayDTO;
import com.ncist.entity.Order;
import com.ncist.entity.Payment;
import com.ncist.entity.PaymentDetail;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.PaymentMapper;
import com.ncist.service.impl.PaymentServiceImpl;
import com.ncist.vo.PaymentQrVO;
import com.ncist.vo.PaymentRecordVO;
import com.ncist.vo.PayResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PaymentServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("支付服务单元测试")
class PaymentServiceImplTest {

    private static final Long USER_ID = 1001L;
    // Luhn 校验有效的测试卡号
    private static final String VALID_CARD = "4111111111111111";
    private static final String INVALID_CARD = "4111111111111112";

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order unpaidOrder(long id) {
        return Order.builder().id(id).orderNo("202410221530001234").userId(USER_ID)
                .totalAmount(1199800).status(1).build();
    }

    private CardPayDTO cardPay(String cardNo) {
        return CardPayDTO.builder()
                .orderId(1L).cardNo(cardNo).expireDate("12/28").cvv("123").build();
    }

    // ================ 卡号支付 ================

    @Test
    @DisplayName("卡号支付-成功：写支付记录且卡号脱敏，订单转已支付")
    void payByCardSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(unpaidOrder(1L));
        when(orderMapper.markPaid(1L, USER_ID)).thenReturn(1);

        PayResultVO vo = paymentService.payByCard(USER_ID, cardPay(VALID_CARD));

        assertEquals(2, vo.getStatus());
        assertNotNull(vo.getPaidAt());
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).insert(captor.capture());
        assertEquals("****1111", captor.getValue().getCardNo());
        assertEquals(1, captor.getValue().getPayMethod());
    }

    @Test
    @DisplayName("卡号支付-Luhn 校验失败返回 400")
    void payByCardLuhnInvalid() {
        when(orderMapper.selectById(1L)).thenReturn(unpaidOrder(1L));

        BusinessException e = assertThrows(BusinessException.class,
                () -> paymentService.payByCard(USER_ID, cardPay(INVALID_CARD)));

        assertEquals(PaymentErrorCode.CARD_INVALID.getCode(), e.getCode());
        verify(paymentMapper, never()).insert(any(Payment.class));
        verify(orderMapper, never()).markPaid(any(), eq(USER_ID));
    }

    @Test
    @DisplayName("卡号支付-订单不存在返回 404")
    void payByCardOrderNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> paymentService.payByCard(USER_ID, cardPay(VALID_CARD)));

        assertEquals(OrderErrorCode.ORDER_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("卡号支付-越权返回 403")
    void payByCardNotOwner() {
        Order other = unpaidOrder(1L);
        other.setUserId(9999L);
        when(orderMapper.selectById(1L)).thenReturn(other);

        BusinessException e = assertThrows(BusinessException.class,
                () -> paymentService.payByCard(USER_ID, cardPay(VALID_CARD)));

        assertEquals(OrderErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("卡号支付-订单非待支付返回 409")
    void payByCardNotPayable() {
        Order paid = unpaidOrder(1L);
        paid.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(paid);

        BusinessException e = assertThrows(BusinessException.class,
                () -> paymentService.payByCard(USER_ID, cardPay(VALID_CARD)));

        assertEquals(PaymentErrorCode.ORDER_NOT_PAYABLE.getCode(), e.getCode());
    }

    // ================ 二维码支付 ================

    @Test
    @DisplayName("获取二维码-成功返回金额与 URL")
    void qrcodeSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(unpaidOrder(1L));

        PaymentQrVO vo = paymentService.getQrcode(USER_ID, 1L);

        assertEquals(1L, vo.getOrderId());
        assertEquals(1199800, vo.getTotalAmount());
        assertTrue(vo.getQrcodeUrl().contains("202410221530001234"));
    }

    @Test
    @DisplayName("确认二维码支付-成功转已支付")
    void confirmQrcodeSuccess() {
        when(orderMapper.selectById(1L)).thenReturn(unpaidOrder(1L));
        when(orderMapper.markPaid(1L, USER_ID)).thenReturn(1);

        PayResultVO vo = paymentService.confirmQrcode(USER_ID, 1L);

        assertEquals(2, vo.getStatus());
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getPayMethod());
    }

    @Test
    @DisplayName("确认二维码支付-并发下已被支付返回 409")
    void confirmQrcodeRace() {
        when(orderMapper.selectById(1L)).thenReturn(unpaidOrder(1L));
        when(orderMapper.markPaid(1L, USER_ID)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> paymentService.confirmQrcode(USER_ID, 1L));

        assertEquals(PaymentErrorCode.ORDER_NOT_PAYABLE.getCode(), e.getCode());
    }

    // ================ 支付记录 ================

    @Test
    @DisplayName("支付记录-返回当前用户的支付流水")
    void myRecordsSuccess() {
        PaymentDetail detail = new PaymentDetail();
        detail.setId(1L);
        detail.setOrderId(1L);
        detail.setOrderNo("202410221530001234");
        detail.setPayMethod(1);
        detail.setCardNo("****1111");
        detail.setAmount(1199800);
        detail.setStatus(1);
        when(paymentMapper.countByUser(USER_ID)).thenReturn(1L);
        when(paymentMapper.selectPageByUser(USER_ID, 0, 10)).thenReturn(List.of(detail));

        PageResult<PaymentRecordVO> result = paymentService.myRecords(USER_ID, 1, 10);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("****1111", result.getList().get(0).getCardNo());
        assertEquals(1199800, result.getList().get(0).getAmount());
    }

    @Test
    @DisplayName("支付记录-无记录时返回空列表")
    void myRecordsEmpty() {
        when(paymentMapper.countByUser(USER_ID)).thenReturn(0L);

        PageResult<PaymentRecordVO> result = paymentService.myRecords(USER_ID, 1, 10);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
        verify(paymentMapper, never()).selectPageByUser(anyLong(), anyInt(), anyInt());
    }
}
