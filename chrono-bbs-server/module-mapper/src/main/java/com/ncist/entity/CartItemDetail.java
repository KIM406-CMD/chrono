package com.ncist.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项联查结果（cart_item + product + product_sku + merchant）
 */
@Data
public class CartItemDetail {

    private Long id;
    private Long userId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createdAt;

    // 商品信息
    private String productName;
    private String productImages;
    private Integer productStatus;

    // SKU 信息
    private String specText;
    private Integer price;
    private Integer stock;

    // 商户信息
    private Long merchantId;
    private String merchantName;
}
