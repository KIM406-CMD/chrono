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
    /** 状态：0=待审核，1=已通过，2=已拒绝，3=待寄回，4=待商家收货，5=退货完成。 */
    private Integer status;
    /** 关联商家 ID。 */
    private Long merchantId;
    /** 退货快递公司。 */
    private String returnCompany;
    /** 退货物流单号。 */
    private String returnLogisticsNo;
    /** 买家寄回时间。 */
    private LocalDateTime returnedAt;
    /** 商家确认收货时间。 */
    private LocalDateTime returnReceivedAt;
    /** 审核人 ID，未审核时为空。 */
    private Long reviewerId;
    /** 审核意见，未审核时为空。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    /** 申请时间。 */
    private LocalDateTime createdAt;
}
