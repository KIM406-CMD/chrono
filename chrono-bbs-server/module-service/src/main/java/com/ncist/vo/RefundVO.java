package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款单出参（api-doc 10.2/10.3）。
 *
 * <p>用户、商家或管理员查看退款单详情与列表时使用。</p>
 */
@Data
@Builder
public class RefundVO {

    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long productId;
    private String productName;
    /** 退款类型：1=仅退款，2=退款+退货。 */
    private Integer type;
    private String reason;
    /** 退款金额，单位：分。 */
    private Integer amount;
    /** 退款状态：0=待审核，1=已通过，2=已拒绝。 */
    private Integer status;
    /** 审核人 ID，未审核时为空。 */
    private Long reviewerId;
    /** 审核意见，未审核时为空。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    /** 申请时间。 */
    private LocalDateTime createdAt;
}
