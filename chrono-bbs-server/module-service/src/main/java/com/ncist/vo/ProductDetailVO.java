package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情。
 *
 * <p>商城端商品详情页使用，包含商品基本信息、店铺信息与 SKU 列表。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

    private Long id;
    private String name;
    private String description;
    /** 商品图片 URL 列表。 */
    private List<String> images;
    private String brand;
    /** 三级分类 ID。 */
    private Long categoryId;
    private String categoryName;
    private Long merchantId;
    private String shopName;
    /** 商品状态：0=下架，1=上架。 */
    private Integer status;
    /** 累计销量。 */
    private Integer sales;
    private List<ProductSkuVO> skuList;
}
