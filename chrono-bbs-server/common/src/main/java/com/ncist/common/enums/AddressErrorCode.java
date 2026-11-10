package com.ncist.common.enums;

import lombok.Getter;

/**
 * 收货地址模块错误码
 */
@Getter
public enum AddressErrorCode implements ErrorCode {

    ADDRESS_NOT_FOUND(404, "收货地址不存在"),
    NO_PERMISSION_OPERATE(403, "无权操作他人地址"),
    ADDRESS_LIMIT_EXCEEDED(400, "地址数量已达上限（最多 20 条）");

    private final Integer code;
    private final String message;

    AddressErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
