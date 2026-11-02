package com.ncist.common.enums;

import lombok.Getter;

/**
 * 商品模块错误码
 */
@Getter
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(404, "商品不存在"),
    NOT_MERCHANT(403, "仅商家可操作"),
    MERCHANT_NOT_APPROVED(403, "商家入驻审核未通过"),
    NO_PERMISSION_OPERATE(403, "无权操作他人商品"),
    SKU_LIST_EMPTY(400, "SKU列表不能为空"),
    SKU_PRICE_ILLEGAL(400, "SKU价格必须大于0"),
    SKU_STOCK_ILLEGAL(400, "SKU库存不能为负数"),
    STATUS_ILLEGAL(400, "商品状态值非法，仅允许0-下架或1-上架"),
    PRODUCT_REFERENCED(409, "商品已被交易引用，无法删除，请下架");

    private final Integer code;
    private final String message;

    ProductErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
