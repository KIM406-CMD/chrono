package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 订单明细实体，对应 {@code order_item} 表。
 *
 * <p>下单时记录商品与价格快照；商品名称、图片等展示信息通过 {@code productId}/{@code skuId}
 * 实时联查，因此商品被删除后历史订单仍可展示已快照的价格与数量。</p>
 */
@Builder
public class OrderItem {

    private Long id;
    /** 所属订单 ID，关联 order 表。 */
    private Long orderId;
    /** 商品 ID，关联 product 表（不设外键约束，允许商品后被删除）。 */
    private Long productId;
    /** SKU ID，关联 product_sku 表。 */
    private Long skuId;
    /** 购买数量。 */
    private Integer quantity;
    /** 下单时单价，单位：分；后续改价不影响该快照。 */
    private Integer unitPrice;
    /** 小计金额，单位：分，等于 unitPrice × quantity。 */
    private Integer subtotal;
}