package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 商品分类实体，对应 {@code category} 表。
 *
 * <p>采用邻接表方式表示三级分类树，{@code parentId} 为 0 表示一级分类，
 * 查询时由业务层在内存中组装树结构。</p>
 */
@Builder
public class Category {

    private Long id;
    /** 分类名称。 */
    private String name;
    /** 父级分类 ID，0 表示一级分类。 */
    private Long parentId;
    /** 分类层级：1-一级，2-二级，3-三级；商品只能挂到三级分类。 */
    private Integer level;
    /** 同级排序序号，值越小越靠前。 */
    private Integer sortOrder;
    /** 分类图标 URL。 */
    private String icon;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}