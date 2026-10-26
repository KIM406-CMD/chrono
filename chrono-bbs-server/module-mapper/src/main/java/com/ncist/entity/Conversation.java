package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    private Long id;
    private Long userId;
    private Long merchantId;
    private Long productId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnread;
    private Integer merchantUnread;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}