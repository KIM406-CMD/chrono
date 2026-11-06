package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款单联查结果（含商品信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundDetail extends Refund {

    private Long productId;
    private String productName;
}
