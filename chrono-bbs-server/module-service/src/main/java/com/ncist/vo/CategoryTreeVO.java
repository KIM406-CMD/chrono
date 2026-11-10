package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 三级分类树节点。
 *
 * <p>商城端展示分类导航树时使用；children 递归承载下级分类。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeVO {

    private Long id;
    private String name;
    /** 分类层级，取值 1/2/3。 */
    private Integer level;
    private String icon;
    /** 子分类列表；叶子节点为空列表或 null。 */
    private List<CategoryTreeVO> children;
}
