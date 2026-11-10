package com.ncist.common.enums;

import lombok.Getter;

/**
 * 商品搜索模块错误码
 */
@Getter
public enum SearchErrorCode implements ErrorCode {

    SEARCH_UNAVAILABLE(503, "搜索服务暂不可用");

    private final Integer code;
    private final String message;

    SearchErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
