package com.ncist.handler;

import com.ncist.common.exception.BusinessException;
import com.ncist.common.enums.CommonErrorCode;
import com.ncist.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>拦截 Controller 层抛出的异常并统一转换为 {@link Result} 结构，
 * 保证前端始终按 {@code code / message / data} 解析响应：
 * 业务异常保留原始业务错误码，参数与请求体错误返回 400，
 * 上传超限返回文件过大错误码，未知异常返回 500 并记录错误日志。
 * HTTP 状态码保持 200，实际结果通过响应体中的 {@code code} 表达。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * <p>记录告警日志，并将异常携带的业务错误码与提示原样返回给调用方，
     * HTTP 状态码仍为 200。</p>
     *
     * @param e 业务异常，包含错误码与面向用户的提示
     * @return 失败响应，{@code code} 为业务错误码
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理请求参数校验失败异常。
     *
     * <p>从 Bean Validation 结果中提取首个校验失败字段及其提示信息，
     * 以 400 业务错误码返回，便于前端定位错误字段。</p>
     *
     * @param e 请求参数校验异常
     * @return 失败响应，{@code code} 为 400，{@code message} 为字段校验提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String field = e.getBindingResult().getFieldError().getField();
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: 字段={}, 原因={}", field, message);
        return Result.error(CommonErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理请求体无法解析异常。
     *
     * <p>请求体缺失、JSON 格式非法或字段类型不匹配时触发，以 400 业务错误码返回。</p>
     *
     * @param e 请求体解析异常
     * @return 失败响应，{@code code} 为 400，提示“请求体格式错误”
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(CommonErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /**
     * 处理上传文件超过 multipart 限制异常。
     *
     * <p>该异常在进入 Controller 前由 Spring 抛出，统一映射为“文件过大”业务错误码 4062。</p>
     *
     * @param e 上传大小超限异常
     * @return 失败响应，{@code code} 为文件过大错误码 4062
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        log.warn("上传文件超限: {}", e.getMessage());
        return Result.error(com.ncist.common.enums.UploadErrorCode.FILE_TOO_LARGE.getCode(),
                com.ncist.common.enums.UploadErrorCode.FILE_TOO_LARGE.getMessage());
    }

    /**
     * 处理未预期的系统异常。
     *
     * <p>记录错误日志与堆栈，对外仅返回统一的 500 错误提示，避免泄露内部细节。</p>
     *
     * @param e 未被其他处理器捕获的异常
     * @return 失败响应，{@code code} 为 500
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(CommonErrorCode.INTERNAL_ERROR.getCode(), CommonErrorCode.INTERNAL_ERROR.getMessage());
    }
}