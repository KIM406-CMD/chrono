package com.ncist.common.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    GUEST(0, "游客"),
    USER(1, "注册用户"),
    MERCHANT(2, "商家"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String name;

    UserRoleEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}