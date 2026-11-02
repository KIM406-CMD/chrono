package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单优惠计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCalculateVO {

    private Integer originalAmount;
    private Integer discountAmount;
    private Integer finalAmount;
    private String couponName;
}
