package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图（对齐前端 Order 类型）。
 *
 * <p>商城端展示订单详情时使用，包含订单主信息、状态时间线与订单项列表。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Long userId;
    /** 收货地址快照（JSON：姓名、电话、省市区、详细地址）。 */
    private String addressSnapshot;
    /** 实付金额，单位：分。 */
    private Integer totalAmount;
    /** 订单状态码：1=待支付，2=已支付，3=已发货，4=已完成，5=已取消，6=退款中，7=已退款。 */
    private Integer status;
    private String remark;
    /** 取消原因，仅已取消订单有值。 */
    private String cancelReason;
    /** 支付时间，未支付时为空。 */
    private LocalDateTime paidAt;
    /** 发货时间，未发货时为空。 */
    private LocalDateTime shippedAt;
    /** 收货时间，未收货时为空。 */
    private LocalDateTime receivedAt;
    /** 完成时间，未完成时为空。 */
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemVO> items;
}
