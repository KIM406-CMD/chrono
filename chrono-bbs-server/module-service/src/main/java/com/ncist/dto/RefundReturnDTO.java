package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 买家填写退货物流信息入参。
 *
 * <p>退货退款审核通过（status=3 待寄回）后，买家填写快递公司与物流单号。</p>
 */
@Data
public class RefundReturnDTO {

    /** 退货快递公司，必填，最长 50 字。 */
    @NotBlank(message = "退货快递公司不能为空")
    @Size(max = 50, message = "快递公司最长 50 字")
    private String returnCompany;

    /** 退货物流单号，必填，最长 50 字。 */
    @NotBlank(message = "退货物流单号不能为空")
    @Size(max = 50, message = "物流单号最长 50 字")
    private String returnLogisticsNo;
}
