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
import java.util.Map;

/**
 * 订单数据访问。
 *
 * <p>订单状态：1-待支付，2-已支付，3-已发货，4-已收货，5-已取消，
 * 6-退款中，7-已退款。所有状态流转更新都带旧状态条件，利用受影响行数判断
 * 是否发生并发竞争，避免重复支付、重复发货或重复退款。</p>
 *
 * <p>管理后台的统计与分页 SQL 位于 {@code resources/mapper/OrderMapper.xml}。</p>
 */
@Mapper
public interface OrderMapper {

    /** 查询订单时的公共列。 */
    String COLUMNS = "id, order_no AS orderNo, user_id AS userId, address_snapshot AS addressSnapshot, "
            + "total_amount AS totalAmount, status, remark, cancel_reason AS cancelReason, "
            + "paid_at AS paidAt, shipped_at AS shippedAt, received_at AS receivedAt, "
            + "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt ";

    /**
     * 新增订单，并回填自增主键。
     *
     * @param order 订单信息；orderNo 需全局唯一，status 通常为 1-待支付
     * @return 受影响行数
     */
    @Insert("INSERT INTO `order` (order_no, user_id, address_snapshot, total_amount, status, remark) "
            + "VALUES (#{orderNo}, #{userId}, #{addressSnapshot}, #{totalAmount}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 按主键查询订单。
     *
     * @param id 订单 ID
     * @return 订单记录，不存在时返回 null
     */
    @Select("SELECT " + COLUMNS + " FROM `order` WHERE id = #{id}")
    Order selectById(Long id);

    /**
     * 按订单号查询订单主键，用于生成订单号时的唯一性校验。
     *
     * @param orderNo 订单编号
     * @return 订单 ID，不存在时返回 null
     */
    @Select("SELECT id FROM `order` WHERE order_no = #{orderNo}")
    Long selectByOrderNo(String orderNo);

    /**
     * 分页查询用户订单。
     *
     * @param userId 用户 ID，通常为当前登录用户
     * @param status 订单状态筛选 1-7，为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 订单列表，按下单时间倒序
     */
    @Select("<script> SELECT " + COLUMNS + " FROM `order` WHERE user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> "
            + "ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size} </script>")
    List<Order> selectPage(@Param("userId") Long userId, @Param("status") Integer status,
                           @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计用户订单数量，用于分页。
     *
     * @param userId 用户 ID
     * @param status 订单状态筛选，为 null 时统计全部
     * @return 订单总数
     */
    @Select("<script> SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} "
            + "<if test='status != null'> AND status = #{status} </if> </script>")
    long countByUser(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 将待支付订单标记为已支付。
     *
     * <p>仅当订单属于该用户且状态为 1-待支付时生效，保证并发下不重复支付。</p>
     *
     * @param id 订单 ID
     * @param userId 用户 ID，用于属主校验
     * @return 受影响行数，0 表示订单不存在、不属于该用户或状态已变更
     */
    @Update("UPDATE `order` SET status = 2, paid_at = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 1")
    int markPaid(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 取消待支付订单并记录取消原因。
     *
     * <p>仅状态为 1-待支付且属于该用户的订单可取消。</p>
     *
     * @param id 订单 ID
     * @param userId 用户 ID，用于属主校验
     * @param reason 取消原因，可为空
     * @return 受影响行数，0 表示订单不存在、不属于该用户或状态不允许取消
     */
    @Update("UPDATE `order` SET status = 5, cancel_reason = #{reason} "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 1")
    int cancel(@Param("id") Long id, @Param("userId") Long userId, @Param("reason") String reason);

    /**
     * 将已支付订单标记为已发货。
     *
     * <p>仅状态为 2-已支付时生效，保证并发下不重复发货；调用方需先校验商家归属。</p>
     *
     * @param id 订单 ID
     * @return 受影响行数，0 表示订单不存在或状态已变更
     */
    @Update("UPDATE `order` SET status = 3, shipped_at = NOW() WHERE id = #{id} AND status = 2")
    int markShipped(@Param("id") Long id);

    /**
     * 将已发货订单标记为已收货。
     *
     * @param id 订单 ID
     * @param userId 用户 ID，用于属主校验
     * @return 受影响行数，0 表示订单不存在、不属于该用户或状态已变更
     */
    @Update("UPDATE `order` SET status = 4, received_at = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 3")
    int markReceived(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 删除用户的已完成或已取消订单（物理删除）。
     *
     * @param id 订单 ID
     * @param userId 用户 ID，用于属主校验
     * @return 受影响行数，0 表示订单不存在、不属于该用户或状态不允许删除
     */
    @Delete("DELETE FROM `order` WHERE id = #{id} AND user_id = #{userId} AND status IN (4, 5)")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将订单标记为退款中。
     *
     * <p>仅已支付、已发货、已收货（2/3/4）的订单可进入退款流程。</p>
     *
     * @param id 订单 ID
     * @return 受影响行数，0 表示订单不存在或状态不允许退款
     */
    @Update("UPDATE `order` SET status = 6, updated_at = NOW() WHERE id = #{id} AND status IN (2, 3, 4)")
    int markRefunding(@Param("id") Long id);

    /**
     * 退款审核通过，将退款中订单标记为已退款。
     *
     * @param id 订单 ID
     * @return 受影响行数，0 表示订单不存在或不在退款中
     */
    @Update("UPDATE `order` SET status = 7, updated_at = NOW() WHERE id = #{id} AND status = 6")
    int markRefunded(@Param("id") Long id);

    /**
     * 退款审核拒绝，将订单从退款中恢复为已收货。
     *
     * @param id 订单 ID
     * @return 受影响行数，0 表示订单不存在或不在退款中
     */
    @Update("UPDATE `order` SET status = 4, updated_at = NOW() WHERE id = #{id} AND status = 6")
    int revertRefunding(@Param("id") Long id);

    // ================ 管理后台（SQL 见 OrderMapper.xml） ================

    /**
     * 管理后台分页查询全平台订单，并联查下单用户名与商品件数。
     *
     * @param orderNo 订单号，精确匹配；为空时不过滤
     * @param status 订单状态筛选 1-7，为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 订单行列表，按创建时间倒序
     */
    List<Map<String, Object>> selectAdminPage(@Param("orderNo") String orderNo, @Param("status") Integer status,
                                              @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计全平台订单数量，用于管理后台分页。
     *
     * @param orderNo 订单号，精确匹配；为空时不过滤
     * @param status 订单状态筛选，为 null 时统计全部
     * @return 订单总数
     */
    long countAdmin(@Param("orderNo") String orderNo, @Param("status") Integer status);

    /**
     * 统计全平台销售额（已支付、已发货、已收货订单）。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 销售额合计，单位：分；无数据时返回 0
     */
    Long sumSalesGlobal(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计全平台订单数量（已支付、已发货、已收货订单）。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 订单数量
     */
    Long countOrdersGlobal(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 全平台 Top 商品统计。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @param limit 返回条数上限
     * @return 行列表，包含 productId、productName、salesCount、totalAmount（单位：分）
     */
    List<Map<String, Object>> selectTopProductsGlobal(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate, @Param("limit") int limit);

    /**
     * 按分类统计全平台销售额。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 行列表，包含 categoryId、categoryName、totalAmount
     */
    List<Map<String, Object>> selectSalesByCategoryGlobal(@Param("startDate") String startDate,
                                                           @Param("endDate") String endDate);

    /**
     * 按商家统计全平台销售额。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 行列表，包含 merchantId、shopName、totalAmount
     */
    List<Map<String, Object>> selectSalesByMerchantGlobal(@Param("startDate") String startDate,
                                                           @Param("endDate") String endDate);

    /**
     * 全平台销售趋势统计。
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @param groupBy 分组粒度：day/week/month，其他值按 day 处理
     * @return 行列表，包含 date 与 amount（单位：分）
     */
    List<Map<String, Object>> selectSalesTrendGlobal(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate, @Param("groupBy") String groupBy);

    /**
     * 统计订单状态分布。
     *
     * @return 行列表，包含 status 与 cnt
     */
    List<Map<String, Object>> selectStatusDistribution();
}
