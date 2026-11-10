package com.ncist.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付记录联查明细。
 *
 * <p>支付记录按用户查询时需联查订单号与订单金额，供 Service 层转换为支付记录视图对象。</p>
 */
@Data
public class PaymentDetail {

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
