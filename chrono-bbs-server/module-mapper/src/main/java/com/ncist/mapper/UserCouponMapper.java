package com.ncist.mapper;

import com.ncist.entity.UserCoupon;
import com.ncist.entity.UserCouponDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户领券数据访问
 */
@Mapper
public interface UserCouponMapper {

    String COLUMNS = "id, user_id AS userId, coupon_id AS couponId, status, "
            + "used_order_id AS usedOrderId, claim_time AS claimTime, used_time AS usedTime, "
            + "created_at AS createdAt ";

    // 按主键查询用户券
    @Select("SELECT " + COLUMNS + " FROM user_coupon WHERE id = #{id}")
    UserCoupon selectById(Long id);

    // 新增领券记录（默认未使用），回填自增主键
    @Insert("INSERT INTO user_coupon (user_id, coupon_id, status) VALUES (#{userId}, #{couponId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCoupon userCoupon);

    // 统计用户对某券的已领数量（per_user_limit 校验）
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countClaimed(@Param("userId") Long userId, @Param("couponId") Long couponId);

    // 我的优惠券联查分页（券名/面额/有效期来自 seckill_coupon）
    @Select("<script> SELECT uc.id, uc.coupon_id AS couponId, sc.name AS couponName, "
            + "sc.discount_type AS discountType, sc.discount_value AS discountValue, sc.min_amount AS minAmount, "
            + "uc.status, sc.end_time AS endTime, uc.claim_time AS claimTime "
            + "FROM user_coupon uc JOIN seckill_coupon sc ON sc.id = uc.coupon_id "
            + "<where> uc.user_id = #{userId} "
            + "<if test='status != null'> AND uc.status = #{status} </if> "
            + "</where> "
            + "ORDER BY uc.claim_time DESC, uc.id DESC LIMIT #{offset}, #{size} </script>")
    List<UserCouponDetail> selectMyPage(@Param("userId") Long userId, @Param("status") Integer status,
                                        @Param("offset") int offset, @Param("size") int size);

    // 统计我的优惠券总数（分页）
    @Select("<script> SELECT COUNT(*) FROM user_coupon "
            + "<where> user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> "
            + "</where> </script>")
    long countMy(@Param("userId") Long userId, @Param("status") Integer status);

    // 定时任务：未使用的用户券随券结束自动过期
    @Update("UPDATE user_coupon SET status = 2 WHERE status = 0 "
            + "AND coupon_id IN (SELECT id FROM seckill_coupon WHERE end_time < NOW())")
    int expireEnded();

    // 下单核销用户券（状态条件保证并发下不重复核销，影响行数 0 即券已被使用）
    @Update("UPDATE user_coupon SET status = 1, used_order_id = #{orderId}, used_time = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int markUsed(@Param("id") Long id, @Param("orderId") Long orderId);

    // 取消订单时按订单回退用户券为未使用
    @Update("UPDATE user_coupon SET status = 0, used_order_id = NULL, used_time = NULL "
            + "WHERE used_order_id = #{orderId} AND status = 1")
    int revokeByOrderId(Long orderId);
}
