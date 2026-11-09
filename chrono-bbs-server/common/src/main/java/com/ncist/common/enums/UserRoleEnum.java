package com.ncist.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举。
 *
 * <p>角色编码与前端角色枚举、数据库 {@code user.role} 字段保持一致。</p>
 */
@Getter
public enum UserRoleEnum {

    /** 游客，未登录状态。 */
    GUEST(0, "游客"),
    /** 注册用户（买家）。 */
    USER(1, "注册用户"),
    /** 商家。 */
    MERCHANT(2, "商家"),
    /** 管理员。 */
    ADMIN(3, "管理员");

    /** 角色编码。 */
    private final Integer code;
    /** 角色名称。 */
    private final String name;

    UserRoleEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}