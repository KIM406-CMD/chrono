package com.ncist.service.impl;

import com.ncist.common.enums.OrderErrorCode;
import com.ncist.common.enums.PaymentErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.dto.CardPayDTO;
import com.ncist.entity.Order;
import com.ncist.entity.Payment;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.PaymentMapper;
import com.ncist.service.PaymentService;
import com.ncist.vo.PaymentQrVO;
import com.ncist.vo.PayResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 支付服务实现（模拟支付）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int PAY_METHOD_CARD = 1;
    private static final int PAY_METHOD_QRCODE = 2;

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVO payByCard(Long userId, CardPayDTO dto) {
        Order order = requirePayableOrder(dto.getOrderId(), userId);
        if (!luhnValid(dto.getCardNo())) {
            throw new BusinessException(PaymentErrorCode.CARD_INVALID);
        }
        LocalDateTime paidAt = LocalDateTime.now();
        paymentMapper.insert(Payment.builder()
                .orderId(order.getId())
                .payMethod(PAY_METHOD_CARD)
                // 卡号脱敏，仅保留后 4 位
                .cardNo("****" + dto.getCardNo().substring(dto.getCardNo().length() - 4))
                .status(1)
                .paidAt(paidAt)
                .build());
        // 状态条件更新，0 行即并发下已被支付
        if (orderMapper.markPaid(order.getId(), userId) == 0) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PAYABLE);
        }
        return PayResultVO.builder().status(2).paidAt(paidAt).build();
    }

    @Override
    public PaymentQrVO getQrcode(Long userId, Long orderId) {
        Order order = requirePayableOrder(orderId, userId);
        String payload = "chrono-order:" + order.getOrderNo() + "|amount:" + order.getTotalAmount();
        String qrcodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                + URLEncoder.encode(payload, StandardCharsets.UTF_8);
        return PaymentQrVO.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .qrcodeUrl(qrcodeUrl)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVO confirmQrcode(Long userId, Long orderId) {
        requirePayableOrder(orderId, userId);
        LocalDateTime paidAt = LocalDateTime.now();
        paymentMapper.insert(Payment.builder()
                .orderId(orderId)
                .payMethod(PAY_METHOD_QRCODE)
                .status(1)
                .paidAt(paidAt)
                .build());
        if (orderMapper.markPaid(orderId, userId) == 0) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PAYABLE);
        }
        return PayResultVO.builder().status(2).paidAt(paidAt).build();
    }

    // ================ 私有校验 ================

    private Order requirePayableOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.NO_PERMISSION_OPERATE);
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PAYABLE);
        }
        return order;
    }

    // Luhn 校验（模拟支付通道的卡号合法性）
    private boolean luhnValid(String cardNo) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = cardNo.length() - 1; i >= 0; i--) {
            int digit = cardNo.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
