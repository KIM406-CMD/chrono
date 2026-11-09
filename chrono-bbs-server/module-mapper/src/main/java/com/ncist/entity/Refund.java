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
 * 退款/退货申请实体，对应 {@code refund} 表。
 *
 * <p>支持部分退款：申请粒度到 {@code orderItemId}，同一订单明细是否存在申请由业务层校验。
 * 审核采用带 {@code status = 0} 条件的更新，避免重复审核。</p>
 */
@Builder
public class Refund {

    private Long id;
    /** 所属订单 ID，关联 order 表。 */
    private Long orderId;
    /** 申请退款的订单明细 ID，关联 order_item 表。 */
    private Long orderItemId;
    /** 申请人用户 ID，关联 user 表。 */
    private Long userId;
    /** 申请类型：1-退款（仅退款），2-退货（退货退款）。 */
    private Integer type;
    /** 申请原因。 */
    private String reason;
    /** 退款金额，单位：分。 */
    private Integer amount;
    /** 审核状态：0-待审核，1-已通过，2-已拒绝。 */
    private Integer status;
    /** 审核人用户 ID（管理员或商家）。 */
    private Long reviewerId;
    /** 审核意见，拒绝时说明原因。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    /** 申请时间。 */
    private LocalDateTime createdAt;
}