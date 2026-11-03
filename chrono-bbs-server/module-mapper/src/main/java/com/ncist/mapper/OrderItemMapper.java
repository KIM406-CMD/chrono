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
 * 订单明细数据访问
 */
@Mapper
public interface OrderItemMapper {

    // 批量写入订单项（下单时价格快照）
    @Insert("<script> INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, subtotal) VALUES "
            + "<foreach collection='list' item='item' separator=','> "
            + "(#{item.orderId}, #{item.productId}, #{item.skuId}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal}) "
            + "</foreach> </script>")
    int insertBatch(@Param("list") List<OrderItem> items);

    @Select("SELECT id, order_id AS orderId, product_id AS productId, sku_id AS skuId, "
            + "quantity, unit_price AS unitPrice, subtotal FROM order_item WHERE order_id = #{orderId} ORDER BY id")
    List<OrderItem> selectByOrderId(Long orderId);

    // 订单项联查商品名称/图片/SKU 规格（详情/列表组装用）
    @Select("SELECT oi.id, oi.order_id AS orderId, oi.product_id AS productId, oi.sku_id AS skuId, "
            + "oi.quantity, oi.unit_price AS unitPrice, oi.subtotal, "
            + "p.name AS productName, p.images AS productImages, s.spec_text AS specText "
            + "FROM order_item oi "
            + "JOIN product p ON oi.product_id = p.id "
            + "JOIN product_sku s ON oi.sku_id = s.id "
            + "WHERE oi.order_id = #{orderId} ORDER BY oi.id")
    List<OrderItemDetail> selectDetailByOrderId(Long orderId);

    // 批量联查多个订单的明细（列表页组装 itemCount/首项信息）
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

    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(Long orderId);
}
