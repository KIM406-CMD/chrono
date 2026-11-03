package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项视图（对齐前端 OrderItem 类型）
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
    private Integer unitPrice;
    private Integer subtotal;
    private String productName;
    private String productImage;
    private String skuSpecText;
}
