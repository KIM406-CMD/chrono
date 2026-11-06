package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantApplyDTO {

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称最多 100 字")
    private String shopName;

    @NotBlank(message = "营业执照不能为空")
    @Size(max = 500, message = "营业执照地址过长")
    private String businessLicense;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
}
