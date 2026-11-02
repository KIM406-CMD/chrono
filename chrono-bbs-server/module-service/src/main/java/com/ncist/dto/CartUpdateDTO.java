package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车数量入参
 */
@Data
public class CartUpdateDTO {

    @NotNull(message = "cartItemId 不能为空")
    private Long cartItemId;

    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 至少为 1")
    @Max(value = 999, message = "单件最多购买 999 件")
    private Integer quantity;
}
