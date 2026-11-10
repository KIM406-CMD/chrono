package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项视图（对齐前端 OrderItem 类型）。
 *
 * <p>订单详情与列表中的商品明细，价格使用下单时的快照。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    /** 下单时单价，单位：分。 */
    private Integer unitPrice;
    /** 小计，单位：分。 */
    private Integer subtotal;
    /** 商品名称快照。 */
    private String productName;
    /** 商品图片 URL 快照。 */
    private String productImage;
    /** 规格展示文本快照，如“红色/XL”。 */
    private String skuSpecText;
}
