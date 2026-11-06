package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款单出参（api-doc 10.2/10.3）
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
    /** 1=仅退款，2=退款+退货 */
    private Integer type;
    private String reason;
    /** 退款金额（分） */
    private Integer amount;
    /** 0=待审核，1=已通过，2=已拒绝 */
    private Integer status;
    private Long reviewerId;
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
