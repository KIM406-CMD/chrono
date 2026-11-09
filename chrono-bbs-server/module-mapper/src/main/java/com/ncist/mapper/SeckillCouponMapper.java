package com.ncist.mapper;

import com.ncist.entity.SeckillCoupon;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀券数据访问。
 *
 * <p>券状态：0-未开始，1-进行中，2-已结束，由定时任务按时间窗刷新。
 * 抢领时先在 Redis 原子扣减，Redis 不可用时降级为
 * {@link #decrementRemain(Long)} 的数据库条件扣减。</p>
 */
@Mapper
public interface SeckillCouponMapper {

    /** 查询秒杀券时的公共列。 */
    String COLUMNS = "id, name, description, "
            + "discount_type AS discountType, discount_value AS discountValue, min_amount AS minAmount, "
            + "total_count AS totalCount, remain_count AS remainCount, start_time AS startTime, end_time AS endTime, "
            + "per_user_limit AS perUserLimit, status, created_at AS createdAt, updated_at AS updatedAt ";

    /**
     * 分页查询可抢的秒杀券。
     *
     * @param status 状态筛选；为 null 时查询未开始与进行中（0、1）的券
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 秒杀券列表，按状态倒序、开始时间升序
     */
    @Select("<script> SELECT " + COLUMNS + " FROM seckill_coupon "
            + "<where> "
            + "<choose> <when test='status != null'> status = #{status} </when> "
            + "<otherwise> status IN (0, 1) </otherwise> </choose> "
            + "</where> "
            + "ORDER BY status DESC, start_time ASC, id DESC LIMIT #{offset}, #{size} </script>")
    List<SeckillCoupon> selectListPage(@Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计可抢秒杀券数量，用于分页。
     *
     * @param status 状态筛选；为 null 时统计未开始与进行中的券
     * @return 券总数
     */
    @Select("<script> SELECT COUNT(*) FROM seckill_coupon "
            + "<where> "
            + "<choose> <when test='status != null'> status = #{status} </when> "
            + "<otherwise> status IN (0, 1) </otherwise> </choose> "
            + "</where> </script>")
    long countList(@Param("status") Integer status);

    /**
     * 按主键查询秒杀券。
     *
     * @param id 秒杀券 ID
     * @return 秒杀券记录，不存在时返回 null
     */
    @Select("SELECT " + COLUMNS + " FROM seckill_coupon WHERE id = #{id}")
    SeckillCoupon selectById(Long id);

    /**
     * 新增秒杀券，并回填自增主键。
     *
     * @param coupon 秒杀券信息；调用方需保证 remainCount 初始等于 totalCount
     * @return 受影响行数
     */
    @Insert("INSERT INTO seckill_coupon (name, description, discount_type, discount_value, min_amount, "
            + "total_count, remain_count, start_time, end_time, per_user_limit, status) "
            + "VALUES (#{name}, #{description}, #{discountType}, #{discountValue}, #{minAmount}, "
            + "#{totalCount}, #{remainCount}, #{startTime}, #{endTime}, #{perUserLimit}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillCoupon coupon);

    /**
     * 动态更新秒杀券基本信息，null 字段不覆盖。
     *
     * <p>不更新发放总量与剩余数量，总量调整需调用 {@link #syncTotalCount(Long, int, int)}。</p>
     *
     * @param coupon 秒杀券信息，id 必填
     * @return 受影响行数，0 表示券不存在
     */
    @Update("<script> UPDATE seckill_coupon <set> "
            + "<if test='name != null'> name = #{name}, </if> "
            + "<if test='description != null'> description = #{description}, </if> "
            + "<if test='discountType != null'> discount_type = #{discountType}, </if> "
            + "<if test='discountValue != null'> discount_value = #{discountValue}, </if> "
            + "<if test='minAmount != null'> min_amount = #{minAmount}, </if> "
            + "<if test='startTime != null'> start_time = #{startTime}, </if> "
            + "<if test='endTime != null'> end_time = #{endTime}, </if> "
            + "<if test='perUserLimit != null'> per_user_limit = #{perUserLimit}, </if> "
            + "<if test='status != null'> status = #{status}, </if> "
            + "</set> WHERE id = #{id} </script>")
    int updateInfo(SeckillCoupon coupon);

    /**
     * 编辑时同步发放总量与剩余数量。
     *
     * <p>剩余数量按 {@code remainCount + delta} 计算，结果小于 0 时截断为 0。</p>
     *
     * @param id 秒杀券 ID
     * @param delta 新总量减旧总量的差值，可为负数
     * @param totalCount 新的发放总量
     * @return 受影响行数，0 表示券不存在
     */
    @Update("UPDATE seckill_coupon SET remain_count = GREATEST(0, remain_count + #{delta}), "
            + "total_count = #{totalCount} WHERE id = #{id}")
    int syncTotalCount(@Param("id") Long id, @Param("delta") int delta, @Param("totalCount") int totalCount);

    /**
     * 原子扣减一张剩余库存，仅当仍有余量时生效。
     *
     * @param id 秒杀券 ID
     * @return 受影响行数；0 表示已抢完或券不存在
     */
    @Update("UPDATE seckill_coupon SET remain_count = remain_count - 1 WHERE id = #{id} AND remain_count > 0")
    int decrementRemain(Long id);

    /**
     * 回补一张剩余库存，用于降级路径或异常补偿。
     *
     * @param id 秒杀券 ID
     * @return 受影响行数，0 表示券不存在
     */
    @Update("UPDATE seckill_coupon SET remain_count = remain_count + 1 WHERE id = #{id}")
    int incrementRemain(Long id);

    /**
     * 按主键删除秒杀券。
     *
     * @param id 秒杀券 ID
     * @return 受影响行数，0 表示券不存在
     */
    @Delete("DELETE FROM seckill_coupon WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 定时任务：将到达开始时间的“未开始”券置为“进行中”。
     *
     * @return 受影响行数，即本次激活的券数量
     */
    @Update("UPDATE seckill_coupon SET status = 1 WHERE status = 0 AND start_time <= NOW()")
    int activateStarted();

    /**
     * 定时任务：将超过结束时间的“进行中”券置为“已结束”。
     *
     * @return 受影响行数，即本次结束的券数量
     */
    @Update("UPDATE seckill_coupon SET status = 2 WHERE status = 1 AND end_time < NOW()")
    int finishEnded();
}
