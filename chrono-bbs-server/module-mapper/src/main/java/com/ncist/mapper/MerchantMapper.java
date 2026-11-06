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
 * 商家数据访问
 */
@Mapper
public interface MerchantMapper {

    String ALL_COLUMNS = "SELECT id, user_id AS userId, shop_name AS shopName, "
            + "business_license AS businessLicense, contact_phone AS contactPhone, "
            + "status, review_reason AS reviewReason, reviewed_at AS reviewedAt, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM merchant ";

    // 按用户 ID 查询商家（商家身份校验用）
    @Select(ALL_COLUMNS + "WHERE user_id = #{userId} LIMIT 1")
    Merchant selectByUserId(Long userId);

    // 按主键查询
    @Select(ALL_COLUMNS + "WHERE id = #{id}")
    Merchant selectById(Long id);

    // 入驻申请（status=0 待审核）
    @Insert("INSERT INTO merchant (user_id, shop_name, business_license, contact_phone, status) "
            + "VALUES (#{userId}, #{shopName}, #{businessLicense}, #{contactPhone}, #{status})")
    int insertApply(Merchant merchant);

    // 商家商品分页（status 不传查全部）
    List<Map<String, Object>> selectMerchantProducts(@Param("merchantId") Long merchantId,
                                                      @Param("status") Integer status,
                                                      @Param("offset") int offset,
                                                      @Param("size") int size);

    long countMerchantProducts(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    // 商家订单 ID 列表（跨 order_item JOIN product 过滤属于该商家的订单）
    List<Long> selectMerchantOrderIds(@Param("merchantId") Long merchantId,
                                       @Param("status") Integer status,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    long countMerchantOrders(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    // 商家订单详情（基础字段 + itemCount，批量 IN 查询）
    List<Map<String, Object>> selectMerchantOrders(@Param("merchantId") Long merchantId,
                                                    @Param("orderIds") List<Long> orderIds);

    // 某订单中属于该商家的商品明细
    List<Map<String, Object>> selectMerchantOrderItems(@Param("merchantId") Long merchantId,
                                                        @Param("orderId") Long orderId);

    // 校验订单商品是否全部属于指定商家（返回 0 表示全部属于，允许发货）
    int countOrderItemsNotBelongTo(@Param("orderId") Long orderId, @Param("merchantId") Long merchantId);

    // 销售统计：总销售额（已支付/已发货/已完成的订单中属于该商家的商品金额汇总）
    Long sumSales(@Param("merchantId") Long merchantId,
                  @Param("startDate") String startDate,
                  @Param("endDate") String endDate);

    Long countOrders(@Param("merchantId") Long merchantId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate);

    // Top 商品：按销量或销售额排序
    List<Map<String, Object>> selectTopProducts(@Param("merchantId") Long merchantId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("limit") int limit);

    // 按分类销售额
    List<Map<String, Object>> selectSalesByCategory(@Param("merchantId") Long merchantId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    // 按日期趋势
    List<Map<String, Object>> selectSalesTrend(@Param("merchantId") Long merchantId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    // 管理后台：商家申请分页（联查申请人用户名）
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

    @Select("<script> SELECT COUNT(*) FROM merchant <if test=\"status != null\"> WHERE status = #{status} </if> </script>")
    long countByStatus(@Param("status") Integer status);

    // 审核更新（status=0 待审核条件保证不重复审核）
    @Update("UPDATE merchant SET status = #{status}, review_reason = #{reviewReason}, "
            + "reviewed_at = NOW(), updated_at = NOW() WHERE id = #{id} AND status = 0")
    int updateReview(@Param("id") Long id, @Param("status") Integer status, @Param("reviewReason") String reviewReason);
}
