package com.ncist.common.enums;

import lombok.Getter;

/**
 * 支付模块错误码
 */
@Getter
public enum PaymentErrorCode implements ErrorCode {

    ORDER_NOT_PAYABLE(409, "订单已支付或状态不允许支付"),
    CARD_INVALID(400, "卡号非法（Luhn 校验不通过）");

    private final Integer code;
    private final String message;

    PaymentErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
