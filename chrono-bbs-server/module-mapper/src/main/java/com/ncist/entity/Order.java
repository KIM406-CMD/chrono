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
 * 订单主记录，对应 {@code order} 表（表名为 MySQL 关键字，SQL 中需使用反引号）。
 *
 * <p>订单状态码：1-待支付，2-已支付（待发货），3-已发货（待收货），
 * 4-已收货（待评价/已完成），5-已取消，6-退款中，7-已退款。
 * 状态流转均采用带旧状态条件的更新，防止并发下重复操作。</p>
 */
@Builder
public class Order {

    private Long id;
    /** 订单编号，全局唯一（唯一索引 uk_order_no），用于对外展示与精确查询。 */
    private String orderNo;
    /** 下单用户 ID，关联 user 表。 */
    private Long userId;
    /** 收货地址快照（JSON：姓名、电话、省市区、详细地址），下单时固化，不受地址后续修改影响。 */
    private String addressSnapshot;
    /** 实付金额，单位：分。 */
    private Integer totalAmount;
    /** 订单状态，取值见类注释；新订单初始为 1-待支付。 */
    private Integer status;
    /** 订单备注，下单时买家填写，可为空。 */
    private String remark;
    /** 取消原因，订单取消（status=5）时记录。 */
    private String cancelReason;
    /** 支付时间。 */
    private LocalDateTime paidAt;
    /** 发货时间。 */
    private LocalDateTime shippedAt;
    /** 收货时间。 */
    private LocalDateTime receivedAt;
    /** 订单完成时间。 */
    private LocalDateTime completedAt;
    /** 下单时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}