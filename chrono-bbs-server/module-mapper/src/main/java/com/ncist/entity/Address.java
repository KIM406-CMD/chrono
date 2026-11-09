package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    private Long id;
    /** 所属用户 ID，关联 user 表；只能操作当前登录用户自己的地址。 */
    private Long userId;
    /** 收货人姓名。 */
    private String receiverName;
    /** 收货联系电话。 */
    private String phone;
    /** 省，纯文本由用户填写。 */
    private String province;
    /** 市，纯文本由用户填写。 */
    private String city;
    /** 区/县，纯文本由用户填写。 */
    private String district;
    /** 详细地址（街道、门牌号等）。 */
    private String detail;
    /** 是否默认地址：0-否，1-是；同一用户至多一条记录为 1。 */
    private Integer isDefault;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
