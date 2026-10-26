package com.ncist.common.enums;

import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(401, "未登录或登录已过期"),
    TOKEN_EXPIRED(401, "token已过期"),
    TOKEN_INVALID(401, "token无效"),
    CAPTCHA_ERROR(400, "验证码错误"),
    USERNAME_EXISTS(409, "用户名已存在"),
    PHONE_EXISTS(409, "手机号已被注册"),
    USERNAME_NOT_FOUND(409, "用户名不存在"),
    PASSWORD_ERROR(409, "密码错误"),
    FORBIDDEN(403, "没有权限"),
    ACCOUNT_DISABLED(403, "账号已被禁用");
    private final Integer code;
    private final String message;

    AuthErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}