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
 * 用户与商家的聊天会话实体，对应 {@code conversation} 表。
 *
 * <p>同一用户与同一商家之间只保留一条会话（唯一索引 uk_user_merchant）；
 * 会话列表按最后消息时间排序。</p>
 */
@Builder
public class Conversation {

    private Long id;
    /** 普通用户 ID，关联 user 表。 */
    private Long userId;
    /** 商家用户 ID，关联 user 表（注意不是 merchant 表主键）。 */
    private Long merchantId;
    /** 发起会话时关联的商品 ID，可为空；会话已存在时忽略该字段。 */
    private Long productId;
    /** 最后一条消息摘要，用于会话列表展示。 */
    private String lastMessage;
    /** 最后一条消息时间，用于会话排序。 */
    private LocalDateTime lastMessageTime;
    /** 用户侧未读消息数，默认 0。 */
    private Integer userUnread;
    /** 商家侧未读消息数，默认 0。 */
    private Integer merchantUnread;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}