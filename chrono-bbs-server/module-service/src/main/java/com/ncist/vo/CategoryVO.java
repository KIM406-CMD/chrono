package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子分类列表项。
 *
 * <p>查询某个父分类下的直接子分类时使用，不包含 children 结构。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    private Long id;
    private String name;
    /** 分类层级，取值 1/2/3。 */
    private Integer level;
    /** 同级排序值，数值越小越靠前。 */
    private Integer sortOrder;
    private String icon;
}
