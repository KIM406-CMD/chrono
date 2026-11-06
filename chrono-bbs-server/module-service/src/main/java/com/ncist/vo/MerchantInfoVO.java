package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantInfoVO {

    private Long id;
    private Long userId;
    private String shopName;
    private String businessLicense;
    private String contactPhone;
    private Integer status;
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
