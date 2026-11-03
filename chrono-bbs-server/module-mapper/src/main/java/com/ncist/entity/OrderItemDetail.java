package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单项联查结果（含商品名称/图片/SKU 规格）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemDetail extends OrderItem {

    private String productName;
    private String productImages;
    private String specText;
}
