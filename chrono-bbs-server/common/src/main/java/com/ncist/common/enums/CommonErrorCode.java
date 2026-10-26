package com.ncist.common.enums;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}