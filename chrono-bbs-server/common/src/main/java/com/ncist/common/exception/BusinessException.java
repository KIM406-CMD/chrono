package com.ncist.common.exception;

import com.ncist.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>用于表达可预期的业务失败（如参数非法、权限不足、状态不允许），
 * 由全局异常处理器转换为统一的 {@code Result} 响应。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，对应 {@link ErrorCode#getCode()}。 */
    private final Integer code;

    /**
     * 使用错误码枚举创建业务异常。
     *
     * @param errorCode 错误码枚举，提供 code 与 message
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用自定义错误码与提示信息创建业务异常。
     *
     * @param code    业务错误码
     * @param message 面向用户的错误提示
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用默认 500 错误码创建业务异常。
     *
     * @param message 面向用户的错误提示
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}