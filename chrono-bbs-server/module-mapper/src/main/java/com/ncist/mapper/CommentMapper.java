package com.ncist.mapper;

import com.ncist.entity.Comment;
import com.ncist.entity.CommentDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论数据访问
 */
@Mapper
public interface CommentMapper {

    String DETAIL_COLUMNS = "c.id, c.product_id AS productId, c.user_id AS userId, "
            + "c.order_item_id AS orderItemId, c.parent_id AS parentId, c.content, "
            + "c.star, c.is_append AS isAppend, c.status, c.created_at AS createdAt, "
            + "u.username, u.nickname, u.avatar ";

    @Insert("INSERT INTO comment (product_id, user_id, order_item_id, parent_id, content, star, is_append, status) "
            + "VALUES (#{productId}, #{userId}, #{orderItemId}, #{parentId}, #{content}, #{star}, #{isAppend}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT id, product_id AS productId, user_id AS userId, order_item_id AS orderItemId, "
            + "parent_id AS parentId, content, star, is_append AS isAppend, status, created_at AS createdAt "
            + "FROM comment WHERE id = #{id}")
    Comment selectById(Long id);

    // 一级评论分页（parent_id 为空且非追评，仅正常状态），联查用户信息
    @Select("<script> SELECT " + DETAIL_COLUMNS + " FROM comment c "
            + "JOIN user u ON c.user_id = u.id "
            + "WHERE c.product_id = #{productId} AND c.parent_id IS NULL AND c.is_append = 0 AND c.status = 0 "
            + "<if test='star != null'> AND c.star = #{star} </if> "
            + "ORDER BY c.created_at DESC, c.id DESC LIMIT #{offset}, #{size} </script>")
    List<CommentDetail> selectFirstPage(@Param("productId") Long productId, @Param("star") Integer star,
                                        @Param("offset") int offset, @Param("size") int size);

    @Select("<script> SELECT COUNT(*) FROM comment c "
            + "WHERE c.product_id = #{productId} AND c.parent_id IS NULL AND c.is_append = 0 AND c.status = 0 "
            + "<if test='star != null'> AND c.star = #{star} </if> </script>")
    long countFirst(@Param("productId") Long productId, @Param("star") Integer star);

    // 批量查子评论（追评/商家回复），联查用户信息
    @Select("<script> SELECT " + DETAIL_COLUMNS + " FROM comment c "
            + "JOIN user u ON c.user_id = u.id "
            + "WHERE c.parent_id IN "
            + "<foreach collection='parentIds' item='pid' open='(' separator=',' close=')'> #{pid} </foreach> "
            + "AND c.status = 0 ORDER BY c.created_at, c.id </script>")
    List<CommentDetail> selectChildrenByParentIds(@Param("parentIds") List<Long> parentIds);

    // 统计某评论下指定类型的子评论（追评唯一性校验用）
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId} AND is_append = #{isAppend} AND status = 0")
    int countChildren(@Param("parentId") Long parentId, @Param("isAppend") Integer isAppend);

    // 该订单明细是否已有首次评价
    @Select("SELECT COUNT(*) FROM comment WHERE order_item_id = #{orderItemId} "
            + "AND parent_id IS NULL AND is_append = 0 AND status = 0")
    int existsByOrderItemId(Long orderItemId);
}
