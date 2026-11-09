package com.ncist.mapper;

import com.ncist.entity.Merchant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 商家数据访问。
 *
 * <p>涵盖商家入驻、审核、商家后台的商品/订单查询与销售统计。
 * 统计类 SQL 位于 {@code resources/mapper/MerchantMapper.xml}，仅统计状态为
 * 已支付、已发货、已收货（2/3/4）的订单。</p>
 */
@Mapper
public interface MerchantMapper {

    /** 查询商家时的公共列。 */
    String ALL_COLUMNS = "SELECT id, user_id AS userId, shop_name AS shopName, "
            + "business_license AS businessLicense, contact_phone AS contactPhone, "
            + "status, review_reason AS reviewReason, reviewed_at AS reviewedAt, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM merchant ";

    /**
     * 按用户 ID 查询商家记录，用于校验当前用户是否具备商家身份。
     *
     * @param userId 用户 ID，关联 user 表
     * @return 商家记录，未入驻时返回 null
     */
    @Select(ALL_COLUMNS + "WHERE user_id = #{userId} LIMIT 1")
    Merchant selectByUserId(Long userId);

    /**
     * 按主键查询商家。
     *
     * @param id 商家 ID（merchant 表主键）
     * @return 商家记录，不存在时返回 null
     */
    @Select(ALL_COLUMNS + "WHERE id = #{id}")
    Merchant selectById(Long id);

    /**
     * 新增商家入驻申请。
     *
     * @param merchant 入驻信息；status 应为 0-待审核，userId 不能重复申请
     * @return 受影响行数
     */
    @Insert("INSERT INTO merchant (user_id, shop_name, business_license, contact_phone, status) "
            + "VALUES (#{userId}, #{shopName}, #{businessLicense}, #{contactPhone}, #{status})")
    int insertApply(Merchant merchant);

    /**
     * 分页查询商家的商品列表（含下架商品）。
     *
     * @param merchantId 商家 ID
     * @param status 商品状态筛选：0-下架，1-上架；为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 商品行列表，字段与 product 表一致
     */
    List<Map<String, Object>> selectMerchantProducts(@Param("merchantId") Long merchantId,
                                                      @Param("status") Integer status,
                                                      @Param("offset") int offset,
                                                      @Param("size") int size);

    /**
     * 统计商家商品数量，用于分页。
     *
     * @param merchantId 商家 ID
     * @param status 商品状态筛选，为 null 时统计全部
     * @return 商品总数
     */
    long countMerchantProducts(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    /**
     * 分页查询包含该商家商品的订单 ID 列表。
     *
     * <p>通过 order_item JOIN product 找出订单，仅返回状态 2-7 的订单，
     * 并去重后按创建时间倒序。</p>
     *
     * @param merchantId 商家 ID
     * @param status 订单状态筛选，为 null 时查全部可见状态
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 订单 ID 列表
     */
    List<Long> selectMerchantOrderIds(@Param("merchantId") Long merchantId,
                                       @Param("status") Integer status,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    /**
     * 统计包含该商家商品的订单数量，用于分页。
     *
     * @param merchantId 商家 ID
     * @param status 订单状态筛选，为 null 时统计全部可见状态
     * @return 订单总数
     */
    long countMerchantOrders(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    /**
     * 批量查询商家订单概要，附带该商家商品件数 itemCount。
     *
     * @param merchantId 商家 ID，用于只统计属于该商家的订单明细
     * @param orderIds 订单 ID 列表，不能为空
     * @return 订单概要行列表，按创建时间倒序
     */
    List<Map<String, Object>> selectMerchantOrders(@Param("merchantId") Long merchantId,
                                                    @Param("orderIds") List<Long> orderIds);

    /**
     * 查询某订单中属于该商家的商品明细。
     *
     * @param merchantId 商家 ID
     * @param orderId 订单 ID
     * @return 商品明细行列表，包含商品名、图片、数量与金额（单位：分）
     */
    List<Map<String, Object>> selectMerchantOrderItems(@Param("merchantId") Long merchantId,
                                                        @Param("orderId") Long orderId);

    /**
     * 统计订单中不属于指定商家的商品明细数量，用于发货前的归属校验。
     *
     * @param orderId 订单 ID
     * @param merchantId 商家 ID
     * @return 不属于该商家的明细数；为 0 表示全部属于该商家，允许发货
     */
    int countOrderItemsNotBelongTo(@Param("orderId") Long orderId, @Param("merchantId") Long merchantId);

    /**
     * 统计商家在指定时间范围内的销售额。
     *
     * <p>仅汇总已支付、已发货、已收货订单中该商家商品的金额。</p>
     *
     * @param merchantId 商家 ID
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空表示不限
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空表示不限
     * @return 销售额合计，单位：分；无数据时返回 0
     */
    Long sumSales(@Param("merchantId") Long merchantId,
                  @Param("startDate") String startDate,
                  @Param("endDate") String endDate);

    /**
     * 统计商家在指定时间范围内的订单数。
     *
     * @param merchantId 商家 ID
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 去重后的订单数量
     */
    Long countOrders(@Param("merchantId") Long merchantId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate);

    /**
     * 查询商家销量/销售额 Top 商品。
     *
     * <p>按销售额倒序返回，结果包含 productId、productName、salesCount、totalAmount。</p>
     *
     * @param merchantId 商家 ID
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @param limit 返回条数上限
     * @return Top 商品行列表
     */
    List<Map<String, Object>> selectTopProducts(@Param("merchantId") Long merchantId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("limit") int limit);

    /**
     * 按分类统计商家销售额。
     *
     * @param merchantId 商家 ID
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 行列表，包含 categoryId、categoryName、totalAmount（单位：分）
     */
    List<Map<String, Object>> selectSalesByCategory(@Param("merchantId") Long merchantId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    /**
     * 按天统计商家销售趋势。
     *
     * @param merchantId 商家 ID
     * @param startDate 开始日期，格式 yyyy-MM-dd，可为空
     * @param endDate 结束日期，格式 yyyy-MM-dd，可为空
     * @return 行列表，包含 date 与 amount（单位：分），按日期升序
     */
    List<Map<String, Object>> selectSalesTrend(@Param("merchantId") Long merchantId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 管理后台分页查询商家申请，并联查申请人用户名。
     *
     * @param status 审核状态筛选：0-待审核，1-已通过，2-已拒绝；为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 商家申请行列表，包含 applicantUsername，按申请时间倒序
     */
    @Select("""
            <script>
            SELECT m.id, m.user_id AS userId, u.username AS applicantUsername,
                   m.shop_name AS shopName, m.contact_phone AS contactPhone,
                   m.status, m.review_reason AS reviewReason,
                   m.reviewed_at AS reviewedAt, m.created_at AS createdAt
            FROM merchant m LEFT JOIN user u ON u.id = m.user_id
            <if test="status != null"> WHERE m.status = #{status} </if>
            ORDER BY m.created_at DESC, m.id DESC LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectByStatusPage(@Param("status") Integer status,
                                                  @Param("offset") int offset,
                                                  @Param("size") int size);

    /**
     * 统计商家申请数量，用于管理后台分页。
     *
     * @param status 审核状态筛选，为 null 时统计全部
     * @return 商家申请总数
     */
    @Select("<script> SELECT COUNT(*) FROM merchant <if test=\"status != null\"> WHERE status = #{status} </if> </script>")
    long countByStatus(@Param("status") Integer status);

    /**
     * 审核商家入驻申请。
     *
     * <p>SQL 带 {@code status = 0} 条件，仅待审核记录可被更新，避免重复审核。</p>
     *
     * @param id 商家 ID
     * @param status 审核结果：1-已通过，2-已拒绝
     * @param reviewReason 审核意见，拒绝时应说明原因
     * @return 受影响行数，0 表示记录不存在或已被审核
     */
    @Update("UPDATE merchant SET status = #{status}, review_reason = #{reviewReason}, "
            + "reviewed_at = NOW(), updated_at = NOW() WHERE id = #{id} AND status = 0")
    int updateReview(@Param("id") Long id, @Param("status") Integer status, @Param("reviewReason") String reviewReason);
}
