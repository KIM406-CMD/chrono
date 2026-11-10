package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单 ID 请求体。
 *
 * <p>用于仅需订单 ID 的订单操作（如支付、确认收货、取消等）的请求体封装。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderIdDTO {

    /** 订单 ID，必填，必须归属当前登录用户（商家/管理员接口按权限校验）。 */
    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;
}
