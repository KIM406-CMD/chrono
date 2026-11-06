package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台-商家申请列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMerchantVO {

    private Long id;
    private Long userId;
    private String applicantUsername;
    private String shopName;
    private String contactPhone;
    private Integer status;
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
