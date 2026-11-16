package com.ncist.mapper;

import com.ncist.entity.Notification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站内通知数据访问。
 *
 * <p>查询与已读操作均以接收用户为条件，保证用户只能读写自己的通知。</p>
 */
@Mapper
public interface NotificationMapper {

    /**
     * 新增通知并回填自增主键。
     *
     * @param notification 通知实体
     * @return 受影响行数
     */
    @Insert("INSERT INTO notification (user_id, type, title, content, related_type, related_id, is_read) "
            + "VALUES (#{userId}, #{type}, #{title}, #{content}, #{relatedType}, #{relatedId}, #{isRead})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    /**
     * 分页查询用户的站内通知。
     *
     * @param userId    用户 ID
     * @param unreadOnly 是否仅查未读，{@code null} 或 {@code false} 表示查全部
     * @param offset    偏移量，从 0 开始
     * @param size      每页条数
     * @return 通知列表，按创建时间倒序
     */
    @Select("<script> SELECT id, user_id AS userId, type, title, content, "
            + "related_type AS relatedType, related_id AS relatedId, is_read AS isRead, created_at AS createdAt "
            + "FROM notification WHERE user_id = #{userId} "
            + "<if test='unreadOnly != null and unreadOnly'> AND is_read = 0 </if> "
            + "ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size} </script>")
    List<Notification> selectPage(@Param("userId") Long userId,
                                  @Param("unreadOnly") Boolean unreadOnly,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    /**
     * 统计用户的站内通知数量，用于分页。
     *
     * @param userId     用户 ID
     * @param unreadOnly 是否仅统计未读
     * @return 通知总数
     */
    @Select("<script> SELECT COUNT(*) FROM notification WHERE user_id = #{userId} "
            + "<if test='unreadOnly != null and unreadOnly'> AND is_read = 0 </if> </script>")
    long countByUser(@Param("userId") Long userId, @Param("unreadOnly") Boolean unreadOnly);

    /**
     * 统计用户未读通知数量。
     *
     * @param userId 用户 ID
     * @return 未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    long countUnread(Long userId);

    /**
     * 将指定通知标记为已读，仅允许操作自己的通知。
     *
     * @param id     通知 ID
     * @param userId 用户 ID
     * @return 受影响行数，0 表示通知不存在或不属于该用户
     */
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将用户全部未读通知标记为已读。
     *
     * @param userId 用户 ID
     * @return 受影响行数
     */
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(Long userId);

    /**
     * 删除用户的全部通知（预留清理能力）。
     *
     * @param userId 用户 ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM notification WHERE user_id = #{userId}")
    int deleteByUser(Long userId);
}
