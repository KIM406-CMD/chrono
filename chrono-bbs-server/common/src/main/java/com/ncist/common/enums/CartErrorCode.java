package com.ncist.common.enums;

import lombok.Getter;

/**
 * 购物车模块错误码
 */
@Getter
public enum CartErrorCode implements ErrorCode {

    SKU_NOT_FOUND(404, "SKU 不存在"),
    PRODUCT_OFF_SHELF(400, "商品已下架，无法加入购物车"),
    STOCK_INSUFFICIENT(400, "库存不足"),
    CART_ITEM_NOT_FOUND(404, "购物车项不存在"),
    NO_PERMISSION_OPERATE(403, "无权操作他人购物车"),
    SELECTED_ILLEGAL(400, "selected 取值非法，仅允许 1/0 或 true/false");

    private final Integer code;
    private final String message;

    CartErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
