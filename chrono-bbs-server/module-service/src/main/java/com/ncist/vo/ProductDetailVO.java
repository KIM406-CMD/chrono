package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

    private Long id;
    private String name;
    private String description;
    private List<String> images;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private Long merchantId;
    private String shopName;
    private Integer status;
    private Integer sales;
    private List<ProductSkuVO> skuList;
}
