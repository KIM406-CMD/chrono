package com.ncist.common.enums;

import lombok.Getter;

/**
 * 退款模块错误码
 */
@Getter
public enum RefundErrorCode implements ErrorCode {

    REFUND_NOT_FOUND(404, "退款单不存在"),
    NO_PERMISSION_OPERATE(403, "无权操作他人退款单"),
    ORDER_ITEM_NOT_FOUND(404, "订单明细不存在"),
    ORDER_NOT_REFUNDABLE(400, "当前订单状态不可申请退款"),
    ALREADY_REFUNDING(409, "该订单明细已存在退款申请"),
    AMOUNT_INVALID(400, "退款金额非法"),
    ALREADY_REVIEWED(409, "退款单已审核"),
    NO_PERMISSION_REVIEW(403, "仅商家或管理员可审核退款"),
    RETURN_STATUS_ILLEGAL(400, "退款单当前状态不可执行退货操作"),
    RETURN_INFO_INVALID(400, "退货物流信息不完整");

    private final Integer code;
    private final String message;

    RefundErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
