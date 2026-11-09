package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车数量入参。
 *
 * <p>商城端调整单个购物车项的数量，数量不得超过对应 SKU 的实时库存。</p>
 */
@Data
public class CartUpdateDTO {

    /** 购物车项 ID，必填，必须归属当前登录用户。 */
    @NotNull(message = "cartItemId 不能为空")
    private Long cartItemId;

    /** 目标数量，必填，取值 1-999，且不能超过对应 SKU 的实时库存。 */
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 至少为 1")
    @Max(value = 999, message = "单件最多购买 999 件")
    private Integer quantity;
}
