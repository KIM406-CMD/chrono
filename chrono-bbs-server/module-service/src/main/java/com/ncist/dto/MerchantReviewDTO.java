package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantReviewDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer approved; // 1=通过，0=拒绝

    @Size(max = 500, message = "审核意见最多 500 字")
    private String reviewReason;
}
