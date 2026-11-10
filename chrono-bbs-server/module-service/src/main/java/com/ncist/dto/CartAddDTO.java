package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车入参。
 *
 * <p>商城端将指定 SKU 加入购物车；同一用户重复加入同一 SKU 时合并数量，
 * 合并后的数量不得超过实时库存。</p>
 *
 * <p>兼容前端多传的 productId：以 skuId 为准（SKU 唯一归属商品），productId 仅做冗余接收。</p>
 */
@Data
public class CartAddDTO {

    /** SKU ID，必填；对应 SKU 必须存在且所属商品处于上架状态。 */
    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    /** 本次加入数量，必填，取值 1-999；与购物车已有数量之和不能超过库存。 */
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 至少为 1")
    @Max(value = 999, message = "单次最多加入 999 件")
    private Integer quantity;

    /** 冗余的商品 ID，可选；服务端忽略该值，以 skuId 推导所属商品。 */
    private Long productId;
}
