package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户券联查载体（user_coupon JOIN seckill_coupon），供 mapper 内部使用。
 *
 * <p>非数据库实体，用于“我的优惠券”列表一次性取出用户券状态与券面信息，
 * 避免业务层二次查询。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponDetail {

    /** 用户券记录 ID（user_coupon.id）。 */
    private Long id;
    /** 秒杀券 ID，关联 seckill_coupon 表。 */
    private Long couponId;
    /** 券名称。 */
    private String couponName;
    /** 优惠类型：1-满减券，2-折扣券。 */
    private Integer discountType;
    /** 优惠值：满减券为减免金额（分），折扣券为折扣值（如 85 表示 8.5 折）。 */
    private Integer discountValue;
    /** 最低消费金额，单位：分；0 表示无门槛。 */
    private Integer minAmount;
    /** 用户券状态：0-未使用，1-已使用，2-已过期。 */
    private Integer status;
    /** 券活动结束时间，用于前端提示有效期。 */
    private LocalDateTime endTime;
    /** 领取时间。 */
    private LocalDateTime claimTime;
}
