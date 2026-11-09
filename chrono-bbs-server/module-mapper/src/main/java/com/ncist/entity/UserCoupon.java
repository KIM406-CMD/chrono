package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户领券记录实体，对应 {@code user_coupon} 表。
 *
 * <p>用户每领取一张秒杀券生成一条记录；{@code (userId, couponId)} 组合用于
 * 校验每人限领数量。券状态随下单核销或活动结束过期而变更。</p>
 */
@Builder
public class UserCoupon {

    private Long id;
    /** 持券用户 ID，关联 user 表。 */
    private Long userId;
    /** 秒杀券 ID，关联 seckill_coupon 表。 */
    private Long couponId;
    /** 使用状态：0-未使用，1-已使用，2-已过期。 */
    private Integer status;
    /** 使用该券的订单 ID，未使用时为空。 */
    private Long usedOrderId;
    /** 领取时间。 */
    private LocalDateTime claimTime;
    /** 使用时间，未使用时为空。 */
    private LocalDateTime usedTime;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
