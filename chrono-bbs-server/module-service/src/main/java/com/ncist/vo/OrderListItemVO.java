package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单列表项视图（对齐前端 OrderListItem 类型）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemVO {

    private Long id;
    private String orderNo;
    private Integer totalAmount;
    private Integer status;
    private LocalDateTime createdAt;
    private Integer itemCount;
    private String firstItemName;
    private String firstItemImage;
}
