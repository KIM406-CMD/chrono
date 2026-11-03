package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建订单结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderVO {

    private Long orderId;
    private String orderNo;
    private Integer totalAmount;
    private Integer status;
    private LocalDateTime createdAt;
}
