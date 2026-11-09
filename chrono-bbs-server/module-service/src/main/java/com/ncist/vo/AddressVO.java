package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址出参（api-doc 8.1）。
 *
 * <p>商城端展示当前用户的收货地址列表与详情；含默认地址标记。</p>
 */
@Data
@Builder
public class AddressVO {

    private Long id;
    /** 所属用户 ID，必须为当前登录用户。 */
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    /** 是否为默认地址：1=默认，0=非默认。 */
    private Integer isDefault;
    private LocalDateTime createdAt;
}
