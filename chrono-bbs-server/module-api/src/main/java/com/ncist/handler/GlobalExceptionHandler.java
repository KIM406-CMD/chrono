package com.ncist.handler;

import com.ncist.common.exception.BusinessException;
import com.ncist.common.enums.CommonErrorCode;
import com.ncist.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String field = e.getBindingResult().getFieldError().getField();
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: 字段={}, 原因={}", field, message);
        return Result.error(CommonErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(CommonErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 上传文件超过 multipart 限制时（进入 Controller 前抛出），映射为业务错误码 4062 */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        log.warn("上传文件超限: {}", e.getMessage());
        return Result.error(com.ncist.common.enums.UploadErrorCode.FILE_TOO_LARGE.getCode(),
                com.ncist.common.enums.UploadErrorCode.FILE_TOO_LARGE.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(CommonErrorCode.INTERNAL_ERROR.getCode(), CommonErrorCode.INTERNAL_ERROR.getMessage());
    }
}