package com.ncist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单优惠计算请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCalculateDTO {

    // 用户优惠券 ID，不传则不使用券
    private Long userCouponId;

    @NotNull(message = "订单原始总金额不能为空")
    @Min(value = 1, message = "订单原始总金额必须大于0")
    private Integer totalAmount;
}
