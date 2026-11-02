package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的优惠券列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCouponVO {

    private Long id;
    private Long couponId;
    private String couponName;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime claimTime;
}
