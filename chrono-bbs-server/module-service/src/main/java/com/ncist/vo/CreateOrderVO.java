package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建订单结果。
 *
 * <p>下单成功后返回订单标识与应付信息，前端据此跳转支付。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderVO {

    private Long orderId;
    private String orderNo;
    /** 订单应付金额，单位：分。 */
    private Integer totalAmount;
    /** 订单状态码：1=待支付，2=已支付，3=已发货，4=已完成，5=已取消，6=退款中，7=已退款。 */
    private Integer status;
    private LocalDateTime createdAt;
}
