package com.ncist.mapper;

import com.ncist.entity.Message;
import com.ncist.entity.MessageDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 消息数据访问
 */
@Mapper
public interface MessageMapper {

    String DETAIL_COLUMNS = "SELECT m.id, m.conversation_id AS conversationId, m.sender_id AS senderId, "
            + "m.sender_role AS senderRole, m.content, m.type, m.is_read AS isRead, "
            + "m.created_at AS createdAt, COALESCE(u.nickname, u.username) AS senderName "
            + "FROM message m LEFT JOIN user u ON u.id = m.sender_id ";

    @Insert("INSERT INTO message (conversation_id, sender_id, sender_role, content, type, is_read) "
            + "VALUES (#{conversationId}, #{senderId}, #{senderRole}, #{content}, #{type}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    // 会话历史消息分页（从新到旧）
    @Select(DETAIL_COLUMNS + "WHERE m.conversation_id = #{conversationId} "
            + "ORDER BY m.created_at DESC, m.id DESC LIMIT #{offset}, #{size}")
    List<MessageDetail> selectPage(@Param("conversationId") Long conversationId,
                                   @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId}")
    long countByConversation(Long conversationId);

    // 把会话中对方发来的消息全部置为已读
    @Update("UPDATE message SET is_read = 1 WHERE conversation_id = #{conversationId} "
            + "AND sender_id <> #{userId} AND is_read = 0")
    int markRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    // 当前用户离线期间的未读消息（接收方为上线的用户）
    @Select(DETAIL_COLUMNS + "JOIN conversation c ON c.id = m.conversation_id "
            + "WHERE (c.user_id = #{userId} OR c.merchant_id = #{userId}) "
            + "AND m.sender_id <> #{userId} AND m.is_read = 0 "
            + "ORDER BY m.created_at ASC, m.id ASC")
    List<MessageDetail> selectUnreadForUser(Long userId);
}
