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
 * 秒杀券数据访问
 */
@Mapper
public interface SeckillCouponMapper {

    String COLUMNS = "id, name, description, "
            + "discount_type AS discountType, discount_value AS discountValue, min_amount AS minAmount, "
            + "total_count AS totalCount, remain_count AS remainCount, start_time AS startTime, end_time AS endTime, "
            + "per_user_limit AS perUserLimit, status, created_at AS createdAt, updated_at AS updatedAt ";

    // 分页查询可抢券：status 不传查全部可抢券（未开始+进行中），传值则精确匹配
    @Select("<script> SELECT " + COLUMNS + " FROM seckill_coupon "
            + "<where> "
            + "<choose> <when test='status != null'> status = #{status} </when> "
            + "<otherwise> status IN (0, 1) </otherwise> </choose> "
            + "</where> "
            + "ORDER BY status DESC, start_time ASC, id DESC LIMIT #{offset}, #{size} </script>")
    List<SeckillCoupon> selectListPage(@Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    // 统计可抢券总数（分页）
    @Select("<script> SELECT COUNT(*) FROM seckill_coupon "
            + "<where> "
            + "<choose> <when test='status != null'> status = #{status} </when> "
            + "<otherwise> status IN (0, 1) </otherwise> </choose> "
            + "</where> </script>")
    long countList(@Param("status") Integer status);

    // 按主键查询
    @Select("SELECT " + COLUMNS + " FROM seckill_coupon WHERE id = #{id}")
    SeckillCoupon selectById(Long id);

    // 新增秒杀券（remain_count = total_count），回填自增主键
    @Insert("INSERT INTO seckill_coupon (name, description, discount_type, discount_value, min_amount, "
            + "total_count, remain_count, start_time, end_time, per_user_limit, status) "
            + "VALUES (#{name}, #{description}, #{discountType}, #{discountValue}, #{minAmount}, "
            + "#{totalCount}, #{remainCount}, #{startTime}, #{endTime}, #{perUserLimit}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillCoupon coupon);

    // 更新基本信息（动态 SET，null 字段不覆盖）
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

    // 编辑时同步总量：remain_count += (新总量 - 旧总量)，负数截断为 0
    @Update("UPDATE seckill_coupon SET remain_count = GREATEST(0, remain_count + #{delta}), "
            + "total_count = #{totalCount} WHERE id = #{id}")
    int syncTotalCount(@Param("id") Long id, @Param("delta") int delta, @Param("totalCount") int totalCount);

    // 原子扣减剩余库存（仅当还有余量时生效），返回受影响行数
    @Update("UPDATE seckill_coupon SET remain_count = remain_count - 1 WHERE id = #{id} AND remain_count > 0")
    int decrementRemain(Long id);

    // 回补剩余库存（降级路径/补偿用）
    @Update("UPDATE seckill_coupon SET remain_count = remain_count + 1 WHERE id = #{id}")
    int incrementRemain(Long id);

    // 删除秒杀券
    @Delete("DELETE FROM seckill_coupon WHERE id = #{id}")
    int deleteById(Long id);

    // 定时任务：未开始 → 进行中
    @Update("UPDATE seckill_coupon SET status = 1 WHERE status = 0 AND start_time <= NOW()")
    int activateStarted();

    // 定时任务：进行中 → 已结束
    @Update("UPDATE seckill_coupon SET status = 2 WHERE status = 1 AND end_time < NOW()")
    int finishEnded();
}
