package com.ncist.mapper;

import com.ncist.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 商家数据访问
 */
@Mapper
public interface MerchantMapper {

    // 按用户 ID 查询商家（商家身份校验用）
    @Select("SELECT id, user_id AS userId, shop_name AS shopName, business_license AS businessLicense, "
            + "contact_phone AS contactPhone, status, review_reason AS reviewReason, "
            + "reviewed_at AS reviewedAt, created_at AS createdAt, updated_at AS updatedAt "
            + "FROM merchant WHERE user_id = #{userId} LIMIT 1")
    Merchant selectByUserId(Long userId);

    // 按主键查询
    @Select("SELECT id, user_id AS userId, shop_name AS shopName, business_license AS businessLicense, "
            + "contact_phone AS contactPhone, status, review_reason AS reviewReason, "
            + "reviewed_at AS reviewedAt, created_at AS createdAt, updated_at AS updatedAt "
            + "FROM merchant WHERE id = #{id}")
    Merchant selectById(Long id);
}
