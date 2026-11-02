package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户券联查载体（user_coupon JOIN seckill_coupon），供 mapper 内部使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponDetail {

    private Long id;
    private Long couponId;
    private String couponName;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;
    private Integer status;
    private LocalDateTime endTime;
    private LocalDateTime claimTime;
}
