package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家入驻审核入参。
 *
 * <p>管理员对待审核的商家申请作出通过或拒绝决定时使用。</p>
 */
@Data
public class MerchantReviewDTO {

    /** 审核结果，必填：1=通过，0=拒绝。 */
    @NotNull(message = "审核结果不能为空")
    private Integer approved; // 1=通过，0=拒绝

    /** 审核意见，可选，最长 500 字；拒绝时建议填写原因。 */
    @Size(max = 500, message = "审核意见最多 500 字")
    private String reviewReason;
}
