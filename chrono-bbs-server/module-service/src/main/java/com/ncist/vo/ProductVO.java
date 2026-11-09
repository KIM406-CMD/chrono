package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品列表项。
 *
 * <p>商城端商品列表、搜索与推荐结果使用，仅展示列表所需摘要信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {

    private Long id;
    private String name;
    /** 商品主图 URL。 */
    private String image;
    private String brand;
    /** 三级分类 ID。 */
    private Long categoryId;
    /** 最低 SKU 价格，单位：分。 */
    private Integer minPrice;
    /** 累计销量。 */
    private Integer sales;
}
