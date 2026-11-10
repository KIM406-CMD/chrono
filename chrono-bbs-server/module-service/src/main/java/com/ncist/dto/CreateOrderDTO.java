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
 * 创建订单请求。
 *
 * <p>商城端用户从购物车选中项提交订单时使用；通过防重复提交 Token 保证幂等，
 * 下单成功后按地址快照与商品快照生成订单。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {

    /** 防重复提交 Token，必填；下单前通过 GET /api/order/submit-token 获取，使用后失效。 */
    @NotBlank(message = "下单 Token 不能为空")
    private String token;

    /** 收货地址 ID，必填，必须归属当前登录用户。 */
    @NotNull(message = "收货地址 ID 不能为空")
    private Long addressId;

    /** 参与结算的购物车项 ID 列表，必填且不能为空。 */
    @NotEmpty(message = "购物车选中项不能为空")
    private List<Long> cartItemIds;

    /** 订单备注，可选，随订单展示给商家。 */
    private String remark;

    /** 用户优惠券 ID，可选；不传或为 null 时不使用优惠券。 */
    private Long userCouponId;
}
