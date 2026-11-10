package com.ncist.mapper;

import com.ncist.entity.Payment;
import com.ncist.entity.PaymentDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付记录数据访问。
 *
 * <p>模拟支付场景下只做写入，支付流水用于追溯订单的支付方式与结果。</p>
 */
@Mapper
public interface PaymentMapper {

    /**
     * 新增一条支付记录。
     *
     * @param payment 支付信息，orderId/payMethod/status 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO payment (order_id, pay_method, card_no, status, paid_at) "
            + "VALUES (#{orderId}, #{payMethod}, #{cardNo}, #{status}, #{paidAt})")
    int insert(Payment payment);

    /**
     * 分页查询当前用户的支付记录，并联查订单号与实付金额。
     *
     * @param userId 用户 ID，仅返回该用户订单对应的支付记录
     * @param offset 偏移量，从 0 开始
     * @param size   每页条数
     * @return 支付记录明细列表，按支付记录创建时间倒序
     */
    @Select("SELECT p.id, p.order_id AS orderId, o.order_no AS orderNo, p.pay_method AS payMethod, "
            + "p.card_no AS cardNo, o.total_amount AS amount, p.status, p.paid_at AS paidAt, "
            + "p.created_at AS createdAt FROM payment p "
            + "JOIN `order` o ON p.order_id = o.id "
            + "WHERE o.user_id = #{userId} "
            + "ORDER BY p.created_at DESC, p.id DESC LIMIT #{offset}, #{size}")
    List<PaymentDetail> selectPageByUser(@Param("userId") Long userId,
                                         @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计当前用户的支付记录数量，用于分页。
     *
     * @param userId 用户 ID
     * @return 支付记录总数
     */
    @Select("SELECT COUNT(*) FROM payment p JOIN `order` o ON p.order_id = o.id "
            + "WHERE o.user_id = #{userId}")
    long countByUser(Long userId);
}
