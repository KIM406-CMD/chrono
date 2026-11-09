package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 聊天消息实体，对应 {@code message} 表。
 *
 * <p>每条消息归属于一个 {@link Conversation}；离线消息先落库，
 * 接收方上线后再通过 WebSocket 推送并标记已读。</p>
 */
@Builder
public class Message {

    private Long id;
    /** 所属会话 ID，关联 conversation 表。 */
    private Long conversationId;
    /** 发送者用户 ID，关联 user 表。 */
    private Long senderId;
    /** 发送者角色：0-用户，1-商家。 */
    private Integer senderRole;
    /** 消息内容；图片消息时存放图片 URL。 */
    private String content;
    /** 消息类型：1-文本，2-图片。 */
    private Integer type;
    /** 是否已读：0-未读，1-已读。 */
    private Integer isRead;
    /** 发送时间。 */
    private LocalDateTime createdAt;
}