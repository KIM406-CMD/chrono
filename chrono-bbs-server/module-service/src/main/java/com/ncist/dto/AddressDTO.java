package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 新增/修改收货地址入参（api-doc 8.3/8.4）。
 *
 * <p>商城端用户维护自己的收货地址时使用；新增与修改共用，修改时地址 ID 由路径参数传入。</p>
 */
@Data
public class AddressDTO {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区/县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否为默认地址：1=默认，0=非默认；不传或为 0 时按非默认处理。 */
    private Integer isDefault;
}
