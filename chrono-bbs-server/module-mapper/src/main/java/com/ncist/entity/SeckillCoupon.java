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
 * 秒杀优惠券实体，对应 {@code seckill_coupon} 表。
 *
 * <p>描述优惠券活动模板与库存；用户领取后生成 {@link UserCoupon} 记录。
 * 抢领时先在 Redis 原子扣减库存，Redis 不可用时降级为数据库条件更新。</p>
 */
@Builder
public class SeckillCoupon {

    private Long id;
    /** 券名称。 */
    private String name;
    /** 券描述。 */
    private String description;
    /** 优惠类型：1-满减券，2-折扣券。 */
    private Integer discountType;
    /** 优惠值：满减券为减免金额（分）；折扣券为折扣值，如 85 表示 8.5 折。 */
    private Integer discountValue;
    /** 最低消费金额，单位：分；0 表示无门槛。 */
    private Integer minAmount;
    /** 发放总量。 */
    private Integer totalCount;
    /** 剩余可领数量；发放总量被编辑时按差值同步，且不会小于 0。 */
    private Integer remainCount;
    /** 活动开始时间。 */
    private LocalDateTime startTime;
    /** 活动结束时间。 */
    private LocalDateTime endTime;
    /** 每人限领数量，默认 1。 */
    private Integer perUserLimit;
    /** 活动状态：0-未开始，1-进行中，2-已结束；由定时任务按时间窗刷新。 */
    private Integer status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
