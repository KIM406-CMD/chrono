package com.ncist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单优惠计算请求。
 *
 * <p>结算页试算优惠券抵扣金额时使用；服务端按券类型与使用门槛计算最终应付金额。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCalculateDTO {

    /** 用户优惠券 ID，可选；不传或为 null 时不使用优惠券。 */
    private Long userCouponId;

    /** 订单原始总金额，单位：分，必填且必须大于 0。 */
    @NotNull(message = "订单原始总金额不能为空")
    @Min(value = 1, message = "订单原始总金额必须大于0")
    private Integer totalAmount;
}
