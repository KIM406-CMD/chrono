package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 商品 SKU 实体，对应 {@code product_sku} 表。
 *
 * <p>商品发布时由商家选择规格模板及规格值组合生成，是价格与库存的最小粒度；
 * 下单时以带库存条件的方式原子扣减。</p>
 */
@Builder
public class ProductSku {

    private Long id;
    /** 所属商品 ID，关联 product 表。 */
    private Long productId;
    /** 规格值 ID 组合（JSON 数组，如 {@code [1,5]} 表示“红色 + XL”）。 */
    private String specIds;
    /** 规格展示文本，如“红色/XL”，便于前端直接展示。 */
    private String specText;
    /** SKU 单价，单位：分。 */
    private Integer price;
    /** 当前库存数量，下单扣减时不允许为负。 */
    private Integer stock;
    /** 启用状态：0-禁用，1-启用；仅启用的 SKU 可加购与下单。 */
    private Integer status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}