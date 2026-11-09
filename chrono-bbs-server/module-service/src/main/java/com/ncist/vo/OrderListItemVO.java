package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单列表项视图（对齐前端 OrderListItem 类型）。
 *
 * <p>商城端“我的订单”列表使用，仅展示首件商品摘要。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemVO {

    private Long id;
    private String orderNo;
    /** 实付金额，单位：分。 */
    private Integer totalAmount;
    /** 订单状态码：1=待支付，2=已支付，3=已发货，4=已完成，5=已取消，6=退款中，7=已退款。 */
    private Integer status;
    private LocalDateTime createdAt;
    /** 订单项数量。 */
    private Integer itemCount;
    private String firstItemName;
    private String firstItemImage;
}
