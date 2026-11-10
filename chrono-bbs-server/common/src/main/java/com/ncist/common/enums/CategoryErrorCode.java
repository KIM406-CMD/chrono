package com.ncist.common.enums;

import lombok.Getter;

/**
 * 分类模块错误码
 */
@Getter
public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(404, "分类不存在");

    private final Integer code;
    private final String message;

    CategoryErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
