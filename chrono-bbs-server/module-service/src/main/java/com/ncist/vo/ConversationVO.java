package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表出参（api-doc 14.1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private Long id;
    private Long userId;
    private Long merchantId;
    private String merchantName;
    private String merchantAvatar;
    private Long productId;
    private String productName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
