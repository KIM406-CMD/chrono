package com.ncist.dto;

import lombok.Data;

/**
 * WebSocket 发送消息报文（api-doc 17.2）
 */
@Data
public class SendMessageDTO {

    /** TEXT / IMAGE */
    private String type;

    private Long conversationId;

    private String content;

    private Long productId;

    /** 客户端时间戳（毫秒） */
    private Long timestamp;
}
