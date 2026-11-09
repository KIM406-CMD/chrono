package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单项联查结果（含商品名称/图片/SKU 规格）。
 *
 * <p>非数据库实体，用于订单详情与订单列表展示：在 {@link OrderItem} 基础上
 * 实时补充当前商品信息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemDetail extends OrderItem {

    /** 商品名称。 */
    private String productName;
    /** 商品图片 URL 数组（JSON 字符串），列表页通常取首张。 */
    private String productImages;
    /** SKU 规格展示文本，如“红色/XL”。 */
    private String specText;
}
