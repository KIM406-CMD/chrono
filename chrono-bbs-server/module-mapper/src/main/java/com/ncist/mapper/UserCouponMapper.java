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
 * 用户领券数据访问。
 *
 * <p>用户券状态：0-未使用，1-已使用，2-已过期。
 * 核销与回退均带状态条件，保证并发下不重复核销、不误改已过期券。</p>
 */
@Mapper
public interface UserCouponMapper {

    /** 查询用户券时的公共列。 */
    String COLUMNS = "id, user_id AS userId, coupon_id AS couponId, status, "
            + "used_order_id AS usedOrderId, claim_time AS claimTime, used_time AS usedTime, "
            + "created_at AS createdAt ";

    /**
     * 按主键查询用户券。
     *
     * @param id 用户券 ID（user_coupon 主键）
     * @return 用户券记录，不存在时返回 null
     */
    @Select("SELECT " + COLUMNS + " FROM user_coupon WHERE id = #{id}")
    UserCoupon selectById(Long id);

    /**
     * 新增领券记录，并回填自增主键。
     *
     * @param userCoupon 用户券信息；status 通常由业务层设为 0-未使用
     * @return 受影响行数
     */
    @Insert("INSERT INTO user_coupon (user_id, coupon_id, status) VALUES (#{userId}, #{couponId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCoupon userCoupon);

    /**
     * 统计用户对某张券的已领数量，用于每人限领数量校验。
     *
     * @param userId 用户 ID
     * @param couponId 秒杀券 ID
     * @return 已领取数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countClaimed(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 分页查询“我的优惠券”，并联查券名、面额与有效期。
     *
     * @param userId 用户 ID，通常为当前登录用户
     * @param status 用户券状态筛选：0-未使用，1-已使用，2-已过期；为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 用户券明细列表，按领取时间倒序
     */
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

    /**
     * 统计“我的优惠券”数量，用于分页。
     *
     * @param userId 用户 ID
     * @param status 用户券状态筛选，为 null 时统计全部
     * @return 用户券总数
     */
    @Select("<script> SELECT COUNT(*) FROM user_coupon "
            + "<where> user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> "
            + "</where> </script>")
    long countMy(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 定时任务：将所属活动已结束的未使用用户券置为已过期。
     *
     * @return 受影响行数，即本次过期的券数量
     */
    @Update("UPDATE user_coupon SET status = 2 WHERE status = 0 "
            + "AND coupon_id IN (SELECT id FROM seckill_coupon WHERE end_time < NOW())")
    int expireEnded();

    /**
     * 下单时核销用户券。
     *
     * <p>仅未使用（status=0）的券可被核销，保证并发下不重复使用。</p>
     *
     * @param id 用户券 ID
     * @param orderId 使用该券的订单 ID
     * @return 受影响行数，0 表示券不存在或已被使用
     */
    @Update("UPDATE user_coupon SET status = 1, used_order_id = #{orderId}, used_time = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int markUsed(@Param("id") Long id, @Param("orderId") Long orderId);

    /**
     * 取消订单时按订单回退已使用的用户券。
     *
     * @param orderId 订单 ID
     * @return 受影响行数，即被回退的券数量
     */
    @Update("UPDATE user_coupon SET status = 0, used_order_id = NULL, used_time = NULL "
            + "WHERE used_order_id = #{orderId} AND status = 1")
    int revokeByOrderId(Long orderId);
}
