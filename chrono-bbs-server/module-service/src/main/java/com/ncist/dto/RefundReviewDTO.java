package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核退款入参（api-doc 10.4）
 */
@Data
public class RefundReviewDTO {

    /** 1=同意，0=拒绝 */
    @NotNull(message = "审核结果不能为空")
    @Min(value = 0, message = "审核结果取值非法")
    @Max(value = 1, message = "审核结果取值非法")
    private Integer approved;

    @Size(max = 500, message = "审核意见最长 500 字")
    private String reviewReason;
}
