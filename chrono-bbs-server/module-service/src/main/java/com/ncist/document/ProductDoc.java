package com.ncist.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品搜索索引文档（Elasticsearch 中的一条商品记录）
 * 字段名与 chrono_product 索引 mapping 保持一致
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDoc {

    private Long id;
    private String name;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    /** 最低 SKU 价格（分） */
    private Integer minPrice;
    private Integer sales;
    private String image;
    /** 0-下架，1-上架；仅上架商品可被搜索 */
    private Integer status;
    /** 创建/上架时间，格式 yyyy-MM-dd HH:mm:ss（用于 time_desc 排序） */
    private String createdAt;
}
