package com.ncist.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回结构。
 *
 * <p>所有对外接口统一返回 {@code code / message / data} 三部分；
 * 成功时 {@code code = 200}，失败时由业务错误码或全局异常处理器填充。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 业务状态码，200 表示成功。 */
    private Integer code;
    /** 面向用户的提示信息。 */
    private String message;
    /** 业务数据，失败时通常为 {@code null}。 */
    private T data;

    /**
     * 创建无数据的成功响应。
     *
     * @param <T> 业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 创建带数据的成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 创建带自定义提示与数据的成功响应。
     *
     * @param message 成功提示
     * @param data    业务数据
     * @param <T>     业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 创建带业务错误码的失败响应。
     *
     * @param code    业务错误码
     * @param message 错误提示
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 创建默认 500 错误码的失败响应。
     *
     * @param message 错误提示
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}