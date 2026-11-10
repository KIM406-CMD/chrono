package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果项（对应 api-doc 15.1 返回结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchItemVO {

    private Long id;
    private String name;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private Integer minPrice;
    private Integer sales;
    private String image;
}
