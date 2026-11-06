package com.ncist.mapper;

import com.ncist.entity.Refund;
import com.ncist.entity.RefundDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 退款数据访问
 */
@Mapper
public interface RefundMapper {

    String DETAIL_COLUMNS = "r.id, r.order_id AS orderId, r.order_item_id AS orderItemId, r.user_id AS userId, "
            + "r.type, r.reason, r.amount, r.status, r.reviewer_id AS reviewerId, "
            + "r.review_reason AS reviewReason, r.reviewed_at AS reviewedAt, r.created_at AS createdAt, "
            + "oi.product_id AS productId, p.name AS productName ";

    @Insert("INSERT INTO refund (order_id, order_item_id, user_id, type, reason, amount, status) "
            + "VALUES (#{orderId}, #{orderItemId}, #{userId}, #{type}, #{reason}, #{amount}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);

    @Select("SELECT " + DETAIL_COLUMNS + " FROM refund r "
            + "JOIN order_item oi ON r.order_item_id = oi.id "
            + "JOIN product p ON oi.product_id = p.id "
            + "WHERE r.id = #{id}")
    RefundDetail selectDetailById(Long id);

    // 我的退款分页
    @Select("SELECT " + DETAIL_COLUMNS + " FROM refund r "
            + "JOIN order_item oi ON r.order_item_id = oi.id "
            + "JOIN product p ON oi.product_id = p.id "
            + "WHERE r.user_id = #{userId} "
            + "ORDER BY r.created_at DESC, r.id DESC LIMIT #{offset}, #{size}")
    List<RefundDetail> selectPageByUser(@Param("userId") Long userId,
                                        @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM refund WHERE user_id = #{userId}")
    long countByUser(Long userId);

    // 该订单明细是否已存在退款申请
    @Select("SELECT COUNT(*) FROM refund WHERE order_item_id = #{orderItemId}")
    int existsByOrderItemId(Long orderItemId);

    // 审核（状态条件更新，0 行即已审核或不存在）
    @Update("UPDATE refund SET status = #{status}, reviewer_id = #{reviewerId}, "
            + "review_reason = #{reviewReason}, reviewed_at = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int markReviewed(@Param("id") Long id, @Param("status") Integer status,
                     @Param("reviewerId") Long reviewerId, @Param("reviewReason") String reviewReason);
}
