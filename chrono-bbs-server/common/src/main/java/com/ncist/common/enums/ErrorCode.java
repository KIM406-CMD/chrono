package com.ncist.common.enums;

/**
 * 业务错误码契约。
 *
 * <p>各业务模块通过枚举实现该接口，统一提供错误码与面向用户的提示信息。</p>
 */
public interface ErrorCode {

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    Integer getCode();

    /**
     * 获取错误提示信息。
     *
     * @return 面向用户的错误提示
     */
    String getMessage();
}