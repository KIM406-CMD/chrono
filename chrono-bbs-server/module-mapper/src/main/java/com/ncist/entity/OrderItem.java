package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer unitPrice;
    private Integer subtotal;
}