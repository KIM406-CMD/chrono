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
 * 评论数据访问。
 *
 * <p>评论查询统一过滤 {@code status = 0} 的正常记录；一级评论与追评/回复分页
 * 分别查询，避免一次性递归拉取整棵评论树。</p>
 */
@Mapper
public interface CommentMapper {

    /** 评论联查用户信息时的公共列，供一级评论与子评论查询复用。 */
    String DETAIL_COLUMNS = "c.id, c.product_id AS productId, c.user_id AS userId, "
            + "c.order_item_id AS orderItemId, c.parent_id AS parentId, c.content, "
            + "c.star, c.is_append AS isAppend, c.status, c.created_at AS createdAt, "
            + "u.username, u.nickname, u.avatar ";

    /**
     * 新增评论，初始状态固定为正常（status=0），并回填自增主键。
     *
     * @param comment 评论内容；一级评论需带 star，追评/回复通过 parentId 关联
     * @return 受影响行数
     */
    @Insert("INSERT INTO comment (product_id, user_id, order_item_id, parent_id, content, star, is_append, status) "
            + "VALUES (#{productId}, #{userId}, #{orderItemId}, #{parentId}, #{content}, #{star}, #{isAppend}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    /**
     * 按主键查询评论。
     *
     * @param id 评论 ID
     * @return 评论记录，不存在时返回 null
     */
    @Select("SELECT id, product_id AS productId, user_id AS userId, order_item_id AS orderItemId, "
            + "parent_id AS parentId, content, star, is_append AS isAppend, status, created_at AS createdAt "
            + "FROM comment WHERE id = #{id}")
    Comment selectById(Long id);

    /**
     * 分页查询商品的一级评论（parentId 为空且非追评，仅正常状态），并联查评论人信息。
     *
     * @param productId 商品 ID
     * @param star 星级筛选 1-5，为 null 时不筛选
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 一级评论明细列表，按时间倒序
     */
    @Select("<script> SELECT " + DETAIL_COLUMNS + " FROM comment c "
            + "JOIN user u ON c.user_id = u.id "
            + "WHERE c.product_id = #{productId} AND c.parent_id IS NULL AND c.is_append = 0 AND c.status = 0 "
            + "<if test='star != null'> AND c.star = #{star} </if> "
            + "ORDER BY c.created_at DESC, c.id DESC LIMIT #{offset}, #{size} </script>")
    List<CommentDetail> selectFirstPage(@Param("productId") Long productId, @Param("star") Integer star,
                                        @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计商品一级评论数量，用于分页。
     *
     * @param productId 商品 ID
     * @param star 星级筛选 1-5，为 null 时不筛选
     * @return 符合条件的评论总数
     */
    @Select("<script> SELECT COUNT(*) FROM comment c "
            + "WHERE c.product_id = #{productId} AND c.parent_id IS NULL AND c.is_append = 0 AND c.status = 0 "
            + "<if test='star != null'> AND c.star = #{star} </if> </script>")
    long countFirst(@Param("productId") Long productId, @Param("star") Integer star);

    /**
     * 批量查询指定父评论下的子评论（追评与商家回复），并联查评论人信息。
     *
     * @param parentIds 父评论 ID 列表，不能为空
     * @return 子评论明细列表，按时间正序；仅包含正常状态记录
     */
    @Select("<script> SELECT " + DETAIL_COLUMNS + " FROM comment c "
            + "JOIN user u ON c.user_id = u.id "
            + "WHERE c.parent_id IN "
            + "<foreach collection='parentIds' item='pid' open='(' separator=',' close=')'> #{pid} </foreach> "
            + "AND c.status = 0 ORDER BY c.created_at, c.id </script>")
    List<CommentDetail> selectChildrenByParentIds(@Param("parentIds") List<Long> parentIds);

    /**
     * 统计某评论下指定类型的子评论数量，用于追评唯一性校验。
     *
     * @param parentId 父评论 ID
     * @param isAppend 子评论类型：0-普通回复，1-追评
     * @return 符合条件的子评论数量
     */
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId} AND is_append = #{isAppend} AND status = 0")
    int countChildren(@Param("parentId") Long parentId, @Param("isAppend") Integer isAppend);

    /**
     * 判断某订单明细是否已有首次评价，防止重复评价。
     *
     * @param orderItemId 订单明细 ID
     * @return 已有首次评价的记录数，大于 0 表示已评价
     */
    @Select("SELECT COUNT(*) FROM comment WHERE order_item_id = #{orderItemId} "
            + "AND parent_id IS NULL AND is_append = 0 AND status = 0")
    int existsByOrderItemId(Long orderItemId);
}
