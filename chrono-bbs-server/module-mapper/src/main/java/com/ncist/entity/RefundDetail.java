package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款单联查结果（含商品信息）。
 *
 * <p>非数据库实体，用于退款列表与详情展示：在 {@link Refund} 基础上
 * 联查订单明细对应的商品 ID 与名称。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundDetail extends Refund {

    /** 商品 ID，经 order_item.product_id 联查得到。 */
    private Long productId;
    /** 商品名称。 */
    private String productName;
}
