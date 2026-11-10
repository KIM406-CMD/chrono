package com.ncist.mapper;

import com.ncist.entity.Conversation;
import com.ncist.entity.ConversationDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话数据访问
 */
@Mapper
public interface ConversationMapper {

    String DETAIL_COLUMNS = "SELECT c.id, c.user_id AS userId, c.merchant_id AS merchantId, "
            + "m.shop_name AS merchantName, u.avatar AS merchantAvatar, "
            + "c.product_id AS productId, p.name AS productName, "
            + "c.last_message AS lastMessage, c.last_message_time AS lastMessageTime, "
            + "c.user_unread AS userUnread, c.merchant_unread AS merchantUnread, "
            + "c.created_at AS createdAt, c.updated_at AS updatedAt "
            + "FROM conversation c "
            + "LEFT JOIN merchant m ON m.user_id = c.merchant_id "
            + "LEFT JOIN user u ON u.id = c.merchant_id "
            + "LEFT JOIN product p ON p.id = c.product_id ";

    @Insert("INSERT INTO conversation (user_id, merchant_id, product_id, user_unread, merchant_unread) "
            + "VALUES (#{userId}, #{merchantId}, #{productId}, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Conversation conversation);

    @Select("SELECT id, user_id AS userId, merchant_id AS merchantId, product_id AS productId, "
            + "last_message AS lastMessage, last_message_time AS lastMessageTime, "
            + "user_unread AS userUnread, merchant_unread AS merchantUnread, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM conversation WHERE user_id = #{userId} AND merchant_id = #{merchantId}")
    Conversation selectByUserAndMerchant(@Param("userId") Long userId, @Param("merchantId") Long merchantId);

    @Select("SELECT id, user_id AS userId, merchant_id AS merchantId, product_id AS productId, "
            + "last_message AS lastMessage, last_message_time AS lastMessageTime, "
            + "user_unread AS userUnread, merchant_unread AS merchantUnread, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM conversation WHERE id = #{id}")
    Conversation selectById(Long id);

    // 当前用户参与的全部会话（买家或商家视角），按最后消息时间倒序
    @Select(DETAIL_COLUMNS + "WHERE c.user_id = #{userId} OR c.merchant_id = #{userId} "
            + "ORDER BY c.last_message_time DESC, c.id DESC")
    List<ConversationDetail> selectDetailByUser(Long userId);

    @Select(DETAIL_COLUMNS + "WHERE c.id = #{id}")
    ConversationDetail selectDetailById(Long id);

    // 发送消息后更新最后消息与对方未读数
    @Update("UPDATE conversation SET last_message = #{lastMessage}, last_message_time = #{lastMessageTime}, "
            + "user_unread = user_unread + #{userUnreadInc}, merchant_unread = merchant_unread + #{merchantUnreadInc}, "
            + "updated_at = #{lastMessageTime} WHERE id = #{id}")
    int updateLastMessage(@Param("id") Long id, @Param("lastMessage") String lastMessage,
                          @Param("lastMessageTime") LocalDateTime lastMessageTime,
                          @Param("userUnreadInc") int userUnreadInc,
                          @Param("merchantUnreadInc") int merchantUnreadInc);

    // 仅清零当前用户自己一侧的未读数
    @Update("UPDATE conversation SET user_unread = IF(user_id = #{userId}, 0, user_unread), "
            + "merchant_unread = IF(merchant_id = #{userId}, 0, merchant_unread), updated_at = NOW() "
            + "WHERE id = #{id}")
    int resetUnread(@Param("id") Long id, @Param("userId") Long userId);

    // 当前用户所有会话的未读总数（买家侧 + 商家侧）
    @Select("SELECT COALESCE(SUM(CASE WHEN user_id = #{userId} THEN user_unread ELSE 0 END), 0) "
            + "+ COALESCE(SUM(CASE WHEN merchant_id = #{userId} THEN merchant_unread ELSE 0 END), 0) "
            + "FROM conversation WHERE user_id = #{userId} OR merchant_id = #{userId}")
    long sumUnread(Long userId);
}
