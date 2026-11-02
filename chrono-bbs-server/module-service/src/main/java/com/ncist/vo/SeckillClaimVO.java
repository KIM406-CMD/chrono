package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 抢领秒杀券结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillClaimVO {

    private Long userCouponId;
    private String couponName;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
