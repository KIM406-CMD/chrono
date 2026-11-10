package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付记录视图对象。
 *
 * <p>用于“我的支付记录”页面展示支付方式、脱敏卡号、金额与支付时间。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordVO {

    /** 支付记录 ID。 */
    private Long id;
    /** 关联订单 ID。 */
    private Long orderId;
    /** 订单号。 */
    private String orderNo;
    /** 支付方式：1-卡号支付，2-二维码支付。 */
    private Integer payMethod;
    /** 银行卡号（仅保留后 4 位），二维码支付时为空。 */
    private String cardNo;
    /** 订单实付金额，单位：分。 */
    private Integer amount;
    /** 支付状态：0-处理中，1-成功，2-失败。 */
    private Integer status;
    /** 支付成功时间，未成功时为空。 */
    private LocalDateTime paidAt;
    /** 支付记录创建时间。 */
    private LocalDateTime createdAt;
}
