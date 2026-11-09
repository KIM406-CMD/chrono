package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的优惠券列表项。
 *
 * <p>用户查看已领取的优惠券及其使用状态时使用。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCouponVO {

    /** 用户券 ID（user_coupon.id），下单选择优惠券时使用。 */
    private Long id;
    /** 关联的秒杀券 ID。 */
    private Long couponId;
    private String couponName;
    /** 优惠类型：1=满减券，2=折扣券。 */
    private Integer discountType;
    /** 优惠值；满减券为减免金额（分），折扣券为折扣百分比（如 85 表示 8.5 折）。 */
    private Integer discountValue;
    /** 最低消费门槛，单位：分；0 表示无门槛。 */
    private Integer minAmount;
    /** 使用状态：0=未使用，1=已使用，2=已过期。 */
    private Integer status;

    /** 有效期截止时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime claimTime;
}
