package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 秒杀券列表/详情。
 *
 * <p>商城端展示可抢领的秒杀券活动时使用。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCouponVO {

    private Long id;
    private String name;
    private String description;
    /** 优惠类型：1=满减券，2=折扣券。 */
    private Integer discountType;
    /** 优惠值；满减券为减免金额（分），折扣券为折扣百分比（如 85 表示 8.5 折）。 */
    private Integer discountValue;
    /** 最低消费门槛，单位：分；0 表示无门槛。 */
    private Integer minAmount;
    /** 发行总量。 */
    private Integer totalCount;
    /** 剩余可抢领数量。 */
    private Integer remainCount;

    /** 活动开始时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 活动结束时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 每人限领数量。 */
    private Integer perUserLimit;
    /** 活动状态：0=未开始，1=进行中，2=已结束。 */
    private Integer status;
    /** 当前用户是否已领取（未登录时为 false）。 */
    private Boolean claimed;
    /** 当前用户已领取数量（未登录时为 0）。 */
    private Integer userClaimCount;
}