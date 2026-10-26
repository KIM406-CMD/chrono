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
public class Message {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private Integer senderRole;
    private String content;
    private Integer type;
    private Integer isRead;
    private LocalDateTime createdAt;
}