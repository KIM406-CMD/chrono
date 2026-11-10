package com.ncist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品上下架请求。
 *
 * <p>商家切换自己商品的上下架状态时使用；下架商品不会在商城展示，也不能加入购物车。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusDTO {

    /** 商品状态，必填：0=下架，1=上架。 */
    private Integer status;
}
