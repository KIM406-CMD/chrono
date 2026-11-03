package com.ncist.common.enums;

import lombok.Getter;

/**
 * 订单模块错误码
 */
@Getter
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "订单不存在"),
    NO_PERMISSION_OPERATE(403, "无权操作他人订单"),
    STATUS_ILLEGAL(400, "订单状态不允许该操作"),
    SUBMIT_TOKEN_INVALID(400, "下单 Token 无效或已使用，请刷新后重试"),
    CART_ITEMS_EMPTY(400, "购物车选中项为空"),
    ORDER_NO_GENERATE_FAILED(500, "订单号生成失败，请重试");

    private final Integer code;
    private final String message;

    OrderErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
