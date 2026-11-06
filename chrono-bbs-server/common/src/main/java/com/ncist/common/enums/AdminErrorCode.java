package com.ncist.common.enums;

import lombok.Getter;

@Getter
public enum AdminErrorCode implements ErrorCode {

    NOT_ADMIN(4051, "仅管理员可执行此操作"),
    CANNOT_DISABLE_ADMIN(4052, "不能禁用管理员账号"),
    CATEGORY_HAS_CHILDREN(4053, "该分类存在子分类，无法删除"),
    CATEGORY_HAS_PRODUCTS(4054, "该分类下存在商品，无法删除"),
    CATEGORY_PARENT_INVALID(4055, "父分类不存在或层级不合法"),
    MERCHANT_ALREADY_REVIEWED(4056, "该商家已完成审核，请勿重复操作");

    private final Integer code;
    private final String message;

    AdminErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
