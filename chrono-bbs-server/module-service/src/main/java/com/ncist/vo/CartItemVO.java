package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项出参（api-doc 5.1 + 前端 CartItem 联查字段）
 */
@Data
@Builder
public class CartItemVO {

    private Long id;
    private Long userId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createdAt;

    // 商品信息
    private String productName;
    private String productImage;

    // SKU 信息（api-doc 命名）
    private String specText;
    private Integer price;
    private Integer stock;

    // SKU 信息（前端命名别名）
    private String skuSpecText;
    private Integer skuPrice;
    private Integer skuStock;

    // 商户信息
    private Long merchantId;
    private String merchantName;
}
