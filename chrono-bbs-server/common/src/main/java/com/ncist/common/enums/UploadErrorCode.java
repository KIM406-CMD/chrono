package com.ncist.common.enums;

import lombok.Getter;

/**
 * 文件上传模块错误码。
 */
@Getter
public enum UploadErrorCode implements ErrorCode {

    FILE_EMPTY(4061, "上传文件不能为空"),
    FILE_TOO_LARGE(4062, "文件大小不能超过 2MB"),
    FILE_TYPE_NOT_ALLOWED(4063, "仅支持 jpg/jpeg/png/webp 格式"),
    UPLOAD_FAILED(4064, "文件上传失败");

    private final Integer code;
    private final String message;

    UploadErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
