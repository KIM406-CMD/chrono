package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息出参（api-doc 14.2 / 17.3）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    /** 0-用户，1-商家 */
    private Integer senderRole;
    private String content;
    /** TEXT / IMAGE */
    private String type;
    private Integer isRead;
    private LocalDateTime createdAt;
}
