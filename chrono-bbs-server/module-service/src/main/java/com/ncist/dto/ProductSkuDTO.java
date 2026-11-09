package com.ncist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建品/改品时的 SKU 项。
 *
 * <p>作为创建/修改商品请求的一部分提交，每个元素对应一个可售规格组合。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDTO {

    /** 规格值 ID 组合，可选；JSON 数组字符串（如 [1,5] 表示“红色 + XL”）。 */
    private String specIds;
    /** 规格展示文本，可选；如“红色/XL”。 */
    private String specText;
    /** 售价，单位：分；创建/修改 SKU 时必填且必须大于 0。 */
    private Integer price;
    /** 库存数量，必填且不能为负数。 */
    private Integer stock;
}
