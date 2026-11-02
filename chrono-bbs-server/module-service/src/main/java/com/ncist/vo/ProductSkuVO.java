package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情页 SKU 项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuVO {

    private Long id;
    private String specIds;
    private String specText;
    private Integer price;
    private Integer stock;
}
