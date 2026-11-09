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
 * 退款数据访问。
 *
 * <p>退款按订单明细粒度申请，支持部分退款；审核采用带
 * {@code status = 0} 条件的状态更新，避免重复审核。</p>
 */
@Mapper
public interface RefundMapper {

    /** 退款联查商品信息时的公共列。 */
    String DETAIL_COLUMNS = "r.id, r.order_id AS orderId, r.order_item_id AS orderItemId, r.user_id AS userId, "
            + "r.type, r.reason, r.amount, r.status, r.reviewer_id AS reviewerId, "
            + "r.review_reason AS reviewReason, r.reviewed_at AS reviewedAt, r.created_at AS createdAt, "
            + "oi.product_id AS productId, p.name AS productName ";

    /**
     * 新增退款申请，初始状态固定为 0-待审核，并回填自增主键。
     *
     * @param refund 申请信息，orderId/orderItemId/userId/type/amount 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO refund (order_id, order_item_id, user_id, type, reason, amount, status) "
            + "VALUES (#{orderId}, #{orderItemId}, #{userId}, #{type}, #{reason}, #{amount}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);

    /**
     * 按主键查询退款详情，并联查商品 ID 与名称。
     *
     * @param id 退款单 ID
     * @return 退款详情，不存在时返回 null
     */
    @Select("SELECT " + DETAIL_COLUMNS + " FROM refund r "
            + "JOIN order_item oi ON r.order_item_id = oi.id "
            + "JOIN product p ON oi.product_id = p.id "
            + "WHERE r.id = #{id}")
    RefundDetail selectDetailById(Long id);

    /**
     * 分页查询用户的退款记录，并联查商品信息。
     *
     * @param userId 用户 ID，通常为当前登录用户
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 退款详情列表，按申请时间倒序
     */
    @Select("SELECT " + DETAIL_COLUMNS + " FROM refund r "
            + "JOIN order_item oi ON r.order_item_id = oi.id "
            + "JOIN product p ON oi.product_id = p.id "
            + "WHERE r.user_id = #{userId} "
            + "ORDER BY r.created_at DESC, r.id DESC LIMIT #{offset}, #{size}")
    List<RefundDetail> selectPageByUser(@Param("userId") Long userId,
                                        @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计用户的退款记录数量，用于分页。
     *
     * @param userId 用户 ID
     * @return 退款记录总数
     */
    @Select("SELECT COUNT(*) FROM refund WHERE user_id = #{userId}")
    long countByUser(Long userId);

    /**
     * 判断某订单明细是否已存在退款申请，防止重复申请。
     *
     * @param orderItemId 订单明细 ID
     * @return 已有申请数量，大于 0 表示已申请
     */
    @Select("SELECT COUNT(*) FROM refund WHERE order_item_id = #{orderItemId}")
    int existsByOrderItemId(Long orderItemId);

    /**
     * 审核退款申请。
     *
     * <p>仅待审核（status=0）的申请可被更新，避免重复审核。</p>
     *
     * @param id 退款单 ID
     * @param status 审核结果：1-已通过，2-已拒绝
     * @param reviewerId 审核人用户 ID（管理员或商家）
     * @param reviewReason 审核意见，拒绝时应说明原因
     * @return 受影响行数，0 表示申请不存在或已被审核
     */
    @Update("UPDATE refund SET status = #{status}, reviewer_id = #{reviewerId}, "
            + "review_reason = #{reviewReason}, reviewed_at = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int markReviewed(@Param("id") Long id, @Param("status") Integer status,
                     @Param("reviewerId") Long reviewerId, @Param("reviewReason") String reviewReason);
}
