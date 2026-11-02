package com.ncist.entity;

import lombok.Data;

/**
 * 商品最低价载体：列表页批量聚合 SKU 最低价用（product_id -> min(price)）
 */
@Data
public class ProductMinPrice {

    private Long productId;
    private Integer minPrice;
}
