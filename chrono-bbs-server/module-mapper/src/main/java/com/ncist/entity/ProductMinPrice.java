package com.ncist.entity;

import lombok.Data;

/**
 * 商品最低价载体：列表页批量聚合 SKU 最低价用（product_id -> min(price)）。
 *
 * <p>非数据库实体，仅承载 {@code GROUP BY product_id} 的聚合查询结果，
 * 用于商品列表展示“起售价”。</p>
 */
@Data
public class ProductMinPrice {

    /** 商品 ID，关联 product 表。 */
    private Long productId;
    /** 该商品全部启用 SKU 中的最低单价，单位：分。 */
    private Integer minPrice;
}
