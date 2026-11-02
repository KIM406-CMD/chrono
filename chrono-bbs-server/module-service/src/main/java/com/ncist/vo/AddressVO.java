package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址出参（api-doc 8.1）
 */
@Data
@Builder
public class AddressVO {

    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private LocalDateTime createdAt;
}
