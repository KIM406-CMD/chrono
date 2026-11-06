package com.ncist.mapper;

import com.ncist.entity.Order;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单数据访问
 */
@Mapper
public interface OrderMapper {

    String COLUMNS = "id, order_no AS orderNo, user_id AS userId, address_snapshot AS addressSnapshot, "
            + "total_amount AS totalAmount, status, remark, cancel_reason AS cancelReason, "
            + "paid_at AS paidAt, shipped_at AS shippedAt, received_at AS receivedAt, "
            + "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt ";

    @Insert("INSERT INTO `order` (order_no, user_id, address_snapshot, total_amount, status, remark) "
            + "VALUES (#{orderNo}, #{userId}, #{addressSnapshot}, #{totalAmount}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT " + COLUMNS + " FROM `order` WHERE id = #{id}")
    Order selectById(Long id);

    // 订单号唯一性校验（生成订单号用）
    @Select("SELECT id FROM `order` WHERE order_no = #{orderNo}")
    Long selectByOrderNo(String orderNo);

    // 用户订单分页（status 不传查全部）
    @Select("<script> SELECT " + COLUMNS + " FROM `order` WHERE user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> "
            + "ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size} </script>")
    List<Order> selectPage(@Param("userId") Long userId, @Param("status") Integer status,
                           @Param("offset") int offset, @Param("size") int size);

    @Select("<script> SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> </script>")
    long countByUser(@Param("userId") Long userId, @Param("status") Integer status);

    // 待支付 → 已支付（状态条件保证并发下不重复支付）
    @Update("UPDATE `order` SET status = 2, paid_at = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 1")
    int markPaid(@Param("id") Long id, @Param("userId") Long userId);

    // 待支付 → 已取消，记录取消原因
    @Update("UPDATE `order` SET status = 5, cancel_reason = #{reason} "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 1")
    int cancel(@Param("id") Long id, @Param("userId") Long userId, @Param("reason") String reason);

    // 已发货 → 已收货
    @Update("UPDATE `order` SET status = 4, received_at = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 3")
    int markReceived(@Param("id") Long id, @Param("userId") Long userId);

    // 仅已完成/已取消的订单可删除（物理删，表无逻辑删除字段）
    @Delete("DELETE FROM `order` WHERE id = #{id} AND user_id = #{userId} AND status IN (4, 5)")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    // 已支付/已发货/已收货 → 退款中
    @Update("UPDATE `order` SET status = 6, updated_at = NOW() WHERE id = #{id} AND status IN (2, 3, 4)")
    int markRefunding(@Param("id") Long id);

    // 退款中 → 已退款（审核通过）
    @Update("UPDATE `order` SET status = 7, updated_at = NOW() WHERE id = #{id} AND status = 6")
    int markRefunded(@Param("id") Long id);

    // 退款中 → 已收货（审核拒绝，恢复原状态）
    @Update("UPDATE `order` SET status = 4, updated_at = NOW() WHERE id = #{id} AND status = 6")
    int revertRefunding(@Param("id") Long id);
}
