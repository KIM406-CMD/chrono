package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单优惠计算结果。
 *
 * <p>结算页试算后返回原始金额、优惠金额与最终应付金额。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCalculateVO {

    /** 原始订单金额，单位：分。 */
    private Integer originalAmount;
    /** 优惠抵扣金额，单位：分。 */
    private Integer discountAmount;
    /** 优惠后应付金额，单位：分。 */
    private Integer finalAmount;
    /** 使用的优惠券名称；未使用券时为空。 */
    private String couponName;
}
