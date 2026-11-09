package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 商家入驻信息实体，对应 {@code merchant} 表。
 *
 * <p>与 {@code user} 表通过 {@code userId} 一对一关联：用户注册为商家并提交入驻申请后，
 * 由管理员审核（status 0 → 1/2）。只有审核通过（status=1）的商家才能发布商品。</p>
 */
@Builder
public class Merchant {

    private Long id;
    /** 关联的用户 ID，关联 user 表，一用户至多一条商家记录。 */
    private Long userId;
    /** 店铺名称。 */
    private String shopName;
    /** 营业执照图片 URL。 */
    private String businessLicense;
    /** 商家联系电话。 */
    private String contactPhone;
    /** 审核状态：0-待审核，1-已通过，2-已拒绝。 */
    private Integer status;
    /** 审核意见，拒绝时说明原因。 */
    private String reviewReason;
    /** 审核时间，未审核时为空。 */
    private LocalDateTime reviewedAt;
    /** 入驻申请时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}