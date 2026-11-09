package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项出参（api-doc 5.1 + 前端 CartItem 联查字段）。
 *
 * <p>商城端展示购物车列表时使用，包含商品、SKU 与商户的联查冗余字段。</p>
 */
@Data
@Builder
public class CartItemVO {

    private Long id;
    private Long userId;
    private Long productId;
    private Long skuId;
    /** 购买数量。 */
    private Integer quantity;
    /** 选中状态：1=选中，0=未选中。 */
    private Integer selected;
    private LocalDateTime createdAt;

    // 商品信息
    private String productName;
    private String productImage;

    // SKU 信息（api-doc 命名）
    /** 规格展示文本，如“红色/XL”。 */
    private String specText;
    /** SKU 单价，单位：分。 */
    private Integer price;
    /** SKU 当前库存。 */
    private Integer stock;

    // SKU 信息（前端命名别名）
    /** 规格展示文本别名（前端字段）。 */
    private String skuSpecText;
    /** SKU 单价别名（前端字段），单位：分。 */
    private Integer skuPrice;
    /** SKU 库存别名（前端字段）。 */
    private Integer skuStock;

    // 商户信息
    private Long merchantId;
    private String merchantName;
}
