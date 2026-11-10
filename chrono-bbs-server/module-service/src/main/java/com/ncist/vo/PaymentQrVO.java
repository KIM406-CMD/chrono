package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付二维码视图（对齐前端 getPaymentQRCode 返回）。
 *
 * <p>选择二维码支付时返回订单应付金额与二维码地址。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQrVO {

    private Long orderId;
    /** 应付金额，单位：分。 */
    private Integer totalAmount;
    /** 支付二维码内容 URL。 */
    private String qrcodeUrl;
}
