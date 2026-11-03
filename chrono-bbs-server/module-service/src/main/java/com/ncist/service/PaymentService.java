package com.ncist.service;

import com.ncist.dto.CardPayDTO;
import com.ncist.vo.PaymentQrVO;
import com.ncist.vo.PayResultVO;

/**
 * 支付服务
 */
public interface PaymentService {

    // 卡号支付（模拟）：校验卡号 Luhn，写支付记录，订单待支付 → 已支付
    PayResultVO payByCard(Long userId, CardPayDTO dto);

    // 获取支付二维码（模拟）
    PaymentQrVO getQrcode(Long userId, Long orderId);

    // 确认二维码支付（模拟）：订单待支付 → 已支付
    PayResultVO confirmQrcode(Long userId, Long orderId);
}
