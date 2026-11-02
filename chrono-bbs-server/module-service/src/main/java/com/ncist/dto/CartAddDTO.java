package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车入参
 * <p>
 * 兼容前端多传的 productId：以 skuId 为准（SKU 唯一归属商品），productId 仅做冗余接收
 */
@Data
public class CartAddDTO {

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 至少为 1")
    @Max(value = 999, message = "单次最多加入 999 件")
    private Integer quantity;

    private Long productId;
}
