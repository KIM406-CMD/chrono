package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 支付记录实体，对应 {@code payment} 表。
 *
 * <p>每次模拟支付生成一条记录，用于支付流水与问题追溯。</p>
 */
@Builder
public class Payment {

    private Long id;
    /** 关联订单 ID，关联 order 表。 */
    private Long orderId;
    /** 支付方式：1-卡号支付，2-二维码支付。 */
    private Integer payMethod;
    /** 银行卡号，模拟支付场景下可为脱敏或明文。 */
    private String cardNo;
    /** 支付状态：0-处理中，1-成功，2-失败。 */
    private Integer status;
    /** 支付成功时间，失败或处理中时为空。 */
    private LocalDateTime paidAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}