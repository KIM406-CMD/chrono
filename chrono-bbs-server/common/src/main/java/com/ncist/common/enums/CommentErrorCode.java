package com.ncist.common.enums;

import lombok.Getter;

/**
 * 评论模块错误码
 */
@Getter
public enum CommentErrorCode implements ErrorCode {

    COMMENT_NOT_FOUND(404, "评论不存在"),
    NO_PERMISSION_OPERATE(403, "无权操作他人评论"),
    ORDER_ITEM_NOT_FOUND(404, "订单明细不存在"),
    ORDER_NOT_RECEIVED(400, "仅已收货订单可评价"),
    PRODUCT_MISMATCH(400, "商品与订单明细不匹配"),
    ALREADY_COMMENTED(409, "该订单明细已评价"),
    COMMENT_NOT_FIRST_LEVEL(400, "仅一级评论可追评"),
    ALREADY_APPENDED(409, "该评论已追加过评价"),
    ONLY_MERCHANT_REPLY(403, "仅商家可回复评论"),
    MERCHANT_INVALID(403, "商家资质无效或未通过审核"),
    PRODUCT_NOT_OWNED(403, "仅可回复自己商品的评论");

    private final Integer code;
    private final String message;

    CommentErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
