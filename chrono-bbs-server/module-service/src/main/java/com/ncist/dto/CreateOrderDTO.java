package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {

    // 防重复提交 Token（下单前通过 GET /api/order/submit-token 获取）
    @NotBlank(message = "下单 Token 不能为空")
    private String token;

    @NotNull(message = "收货地址 ID 不能为空")
    private Long addressId;

    @NotEmpty(message = "购物车选中项不能为空")
    private List<Long> cartItemIds;

    private String remark;

    // 用户优惠券 ID，不传则不使用券
    private Long userCouponId;
}
