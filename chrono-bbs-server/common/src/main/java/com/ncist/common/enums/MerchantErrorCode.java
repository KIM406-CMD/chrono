package com.ncist.common.enums;

import lombok.Getter;

/**
 * 商家模块错误码。
 */
@Getter
public enum MerchantErrorCode implements ErrorCode {

    MERCHANT_NOT_FOUND(4041, "商家不存在"),
    MERCHANT_STATUS_ILLEGAL(4042, "商家状态不允许此操作"),
    ALREADY_MERCHANT(4043, "您已提交过入驻申请"),
    NOT_MERCHANT(4044, "您不是商家，无权操作"),
    ORDER_NOT_BELONG_TO_YOU(4045, "该订单不属于您的店铺"),
    SHIPPED_STATUS_ILLEGAL(4046, "订单状态不允许发货"),
    NO_PRODUCTS(4047, "商家暂无商品");

    private final Integer code;
    private final String message;

    MerchantErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
