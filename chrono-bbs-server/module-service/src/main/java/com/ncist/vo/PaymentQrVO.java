package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付二维码视图（对齐前端 getPaymentQRCode 返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQrVO {

    private Long orderId;
    private Integer totalAmount;
    private String qrcodeUrl;
}
