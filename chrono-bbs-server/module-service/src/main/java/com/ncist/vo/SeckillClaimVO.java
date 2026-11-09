package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 抢领秒杀券结果。
 *
 * <p>用户成功抢领秒杀券后返回用户券 ID 与券面信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillClaimVO {

    /** 用户券 ID（user_coupon.id），下单选择优惠券时使用。 */
    private Long userCouponId;
    private String couponName;
    /** 优惠类型：1=满减券，2=折扣券。 */
    private Integer discountType;
    /** 优惠值；满减券为减免金额（分），折扣券为折扣百分比（如 85 表示 8.5 折）。 */
    private Integer discountValue;
    /** 最低消费门槛，单位：分；0 表示无门槛。 */
    private Integer minAmount;

    /** 有效期截止时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
