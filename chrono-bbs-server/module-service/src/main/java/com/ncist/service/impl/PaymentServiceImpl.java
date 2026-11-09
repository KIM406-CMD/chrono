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
 * 支付服务实现（模拟支付）。
 *
 * <p>支付记录写入与订单状态更新在同一事务中完成；订单状态通过
 * {@code WHERE status = 待支付} 的条件更新，避免并发下重复支付。
 * 银行卡号仅保留后 4 位存储，二维码内容包含订单号与金额。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /** 支付方式：银行卡。 */
    private static final int PAY_METHOD_CARD = 1;
    /** 支付方式：二维码。 */
    private static final int PAY_METHOD_QRCODE = 2;

    /** 订单数据访问组件。 */
    private final OrderMapper orderMapper;
    /** 支付记录数据访问组件。 */
    private final PaymentMapper paymentMapper;

    /**
     * {@inheritDoc}
     *
     * <p>先校验订单可支付与卡号 Luhn 合法性，再写入脱敏支付记录并条件更新订单状态；
     * 状态更新影响 0 行时抛异常回滚。</p>
     */
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

    /** {@inheritDoc} */
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

    /**
     * {@inheritDoc}
     *
     * <p>写入二维码支付记录后条件更新订单状态，更新 0 行时抛异常回滚。</p>
     */
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

    /**
     * 查询订单并校验属主与可支付状态（待支付）。
     *
     * @param orderId 订单 ID
     * @param userId  当前用户 ID
     * @return 订单实体
     * @throws BusinessException 订单不存在、不属于当前用户或状态不是待支付时抛出
     */
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

    /**
     * 使用 Luhn 算法校验银行卡号合法性（模拟支付通道校验）。
     *
     * @param cardNo 银行卡号
     * @return 校验位合法时返回 true
     */
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
