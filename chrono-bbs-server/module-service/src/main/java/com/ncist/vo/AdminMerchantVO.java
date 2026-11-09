package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台-商家申请列表项。
 *
 * <p>管理员分页查看商家入驻申请时使用。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMerchantVO {

    private Long id;
    /** 申请人的用户 ID。 */
    private Long userId;
    private String applicantUsername;
    private String shopName;
    private String contactPhone;
    /** 审核状态：0=待审核，1=已通过，2=已拒绝。 */
    private Integer status;
    /** 审核意见，未审核时为空。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
