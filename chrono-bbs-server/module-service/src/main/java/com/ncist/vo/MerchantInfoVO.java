package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商家信息出参。
 *
 * <p>商家查看自己的入驻信息，或管理后台查看指定商家详情时使用。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantInfoVO {

    private Long id;
    private Long userId;
    private String shopName;
    /** 营业执照图片 URL。 */
    private String businessLicense;
    private String contactPhone;
    /** 审核状态：0=待审核，1=已通过，2=已拒绝。 */
    private Integer status;
    /** 审核意见，未审核时为空。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
