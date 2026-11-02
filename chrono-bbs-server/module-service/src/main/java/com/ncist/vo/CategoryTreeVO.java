package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 三级分类树节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeVO {

    private Long id;
    private String name;
    private Integer level;
    private String icon;
    private List<CategoryTreeVO> children;
}
