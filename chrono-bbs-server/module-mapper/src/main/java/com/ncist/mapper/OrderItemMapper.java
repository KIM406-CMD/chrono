package com.ncist.mapper;

import com.ncist.entity.OrderItem;
import com.ncist.entity.OrderItemDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细数据访问。
 *
 * <p>下单时批量写入价格快照；查询时联查商品与 SKU 获取展示信息，
 * 商品后续被删除不影响已保存的金额与数量。</p>
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 批量写入订单明细（下单时保存单价与小计快照）。
     *
     * @param items 订单明细列表，不能为空；各元素的 orderId 必须已存在
     * @return 受影响行数
     */
    @Insert("<script> INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, subtotal) VALUES "
            + "<foreach collection='list' item='item' separator=','> "
            + "(#{item.orderId}, #{item.productId}, #{item.skuId}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal}) "
            + "</foreach> </script>")
    int insertBatch(@Param("list") List<OrderItem> items);

    /**
     * 查询指定订单的全部明细，按 ID 升序。
     *
     * @param orderId 订单 ID
     * @return 订单明细列表，无记录时返回空列表
     */
    @Select("SELECT id, order_id AS orderId, product_id AS productId, sku_id AS skuId, "
            + "quantity, unit_price AS unitPrice, subtotal FROM order_item WHERE order_id = #{orderId} ORDER BY id")
    List<OrderItem> selectByOrderId(Long orderId);

    /**
     * 按主键查询订单明细。
     *
     * @param id 订单明细 ID
     * @return 订单明细，不存在时返回 null
     */
    @Select("SELECT id, order_id AS orderId, product_id AS productId, sku_id AS skuId, "
            + "quantity, unit_price AS unitPrice, subtotal FROM order_item WHERE id = #{id}")
    OrderItem selectById(Long id);

    /**
     * 查询订单明细并联查商品名称、图片与 SKU 规格，用于订单详情。
     *
     * @param orderId 订单 ID
     * @return 明细展示列表，按明细 ID 升序
     */
    @Select("SELECT oi.id, oi.order_id AS orderId, oi.product_id AS productId, oi.sku_id AS skuId, "
            + "oi.quantity, oi.unit_price AS unitPrice, oi.subtotal, "
            + "p.name AS productName, p.images AS productImages, s.spec_text AS specText "
            + "FROM order_item oi "
            + "JOIN product p ON oi.product_id = p.id "
            + "JOIN product_sku s ON oi.sku_id = s.id "
            + "WHERE oi.order_id = #{orderId} ORDER BY oi.id")
    List<OrderItemDetail> selectDetailByOrderId(Long orderId);

    /**
     * 批量查询多个订单的明细，用于订单列表组装商品件数与首项信息。
     *
     * @param orderIds 订单 ID 列表，不能为空
     * @return 明细展示列表，按订单 ID、明细 ID 升序
     */
    @Select("<script> SELECT oi.id, oi.order_id AS orderId, oi.product_id AS productId, oi.sku_id AS skuId, "
            + "oi.quantity, oi.unit_price AS unitPrice, oi.subtotal, "
            + "p.name AS productName, p.images AS productImages, s.spec_text AS specText "
            + "FROM order_item oi "
            + "JOIN product p ON oi.product_id = p.id "
            + "JOIN product_sku s ON oi.sku_id = s.id "
            + "WHERE oi.order_id IN "
            + "<foreach collection='orderIds' item='oid' open='(' separator=',' close=')'> #{oid} </foreach> "
            + "ORDER BY oi.order_id, oi.id </script>")
    List<OrderItemDetail> selectDetailByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 按订单 ID 删除全部明细。
     *
     * <p>仅允许删除已完成或已取消订单的调用方使用（业务层负责校验）。</p>
     *
     * @param orderId 订单 ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(Long orderId);
}
