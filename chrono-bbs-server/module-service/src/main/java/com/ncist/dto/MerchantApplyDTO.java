package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家入驻申请入参。
 *
 * <p>普通用户提交店铺信息申请成为商家；提交后进入待审核状态，由管理员审核。</p>
 */
@Data
public class MerchantApplyDTO {

    /** 店铺名称，必填，最长 100 字。 */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称最多 100 字")
    private String shopName;

    /** 营业执照图片 URL，必填，最长 500 字符。 */
    @NotBlank(message = "营业执照不能为空")
    @Size(max = 500, message = "营业执照地址过长")
    private String businessLicense;

    /** 联系电话，必填，11 位手机号。 */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
}
