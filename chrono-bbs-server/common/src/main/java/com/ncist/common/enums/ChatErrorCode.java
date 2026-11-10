package com.ncist.common.enums;

import lombok.Getter;

/**
 * 即时通讯模块错误码
 */
@Getter
public enum ChatErrorCode implements ErrorCode {

    CONVERSATION_NOT_FOUND(404, "会话不存在"),
    NO_PERMISSION_ACCESS(403, "无权访问该会话"),
    MERCHANT_NOT_FOUND(404, "商家不存在"),
    MERCHANT_INVALID(403, "商家资质无效或未通过审核"),
    CANNOT_CHAT_SELF(400, "不能与自己发起会话"),
    MESSAGE_CONTENT_EMPTY(400, "消息内容不能为空"),
    MESSAGE_TYPE_INVALID(400, "消息类型非法"),
    MESSAGE_TOO_LONG(400, "消息内容最长 2000 字");

    private final Integer code;
    private final String message;

    ChatErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
