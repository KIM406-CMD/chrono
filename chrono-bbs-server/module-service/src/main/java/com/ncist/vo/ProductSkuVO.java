package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情页 SKU 项。
 *
 * <p>展示可售规格组合及其价格、库存。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuVO {

    private Long id;
    /** 规格值 ID 组合（JSON 数组字符串）。 */
    private String specIds;
    /** 规格展示文本，如“红色/XL”。 */
    private String specText;
    /** 售价，单位：分。 */
    private Integer price;
    /** 当前库存。 */
    private Integer stock;
}
