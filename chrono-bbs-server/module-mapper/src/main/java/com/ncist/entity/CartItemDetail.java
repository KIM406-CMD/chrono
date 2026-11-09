package com.ncist.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项联查结果（cart_item + product + product_sku + merchant）。
 *
 * <p>非数据库实体，仅用于购物车列表、结算校验等场景一次性取出商品、SKU 与商家信息，
 * 避免业务层多次查询。</p>
 */
@Data
public class CartItemDetail {

    private Long id;
    /** 所属用户 ID，关联 cart_item.user_id。 */
    private Long userId;
    /** 商品 ID。 */
    private Long productId;
    /** SKU ID。 */
    private Long skuId;
    /** 购物车中的购买数量。 */
    private Integer quantity;
    /** 是否选中参与结算：0-未选中，1-已选中。 */
    private Integer selected;
    /** 加入购物车时间。 */
    private LocalDateTime createdAt;

    // 商品信息
    /** 商品名称。 */
    private String productName;
    /** 商品图片 URL 数组（JSON 字符串，最多 4 张）。 */
    private String productImages;
    /** 商品上下架状态：0-下架，1-上架；下单前需校验为 1。 */
    private Integer productStatus;

    // SKU 信息
    /** SKU 规格展示文本，如“红色/XL”。 */
    private String specText;
    /** SKU 单价，单位：分。 */
    private Integer price;
    /** SKU 当前库存数量。 */
    private Integer stock;

    // 商户信息
    /** 商家 ID，对应 merchant 表主键。 */
    private Long merchantId;
    /** 店铺名称。 */
    private String merchantName;
}
