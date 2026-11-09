package com.ncist.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类新增/修改共用入参。
 *
 * <p>新增：name、level 必填；parentId 一级分类传 0 或不传。<br>
 * 修改：仅 name/sortOrder/icon 生效，parentId/level 不允许变更（防止打乱树结构）。</p>
 */
@Data
public class CategoryUpsertDTO {

    /** 分类名称，新增时必填，最长 50 字。 */
    @Size(max = 50, message = "分类名称最多 50 字")
    private String name;

    /** 父分类 ID，可选；一级分类传 0 或不传。 */
    private Long parentId;

    /** 分类层级，新增时必填，取值 1/2/3，分别表示一级、二级、三级分类。 */
    private Integer level; // 1/2/3

    /** 同级排序值，可选；数值越小越靠前。 */
    private Integer sortOrder;

    /** 分类图标 URL，可选，最长 255 字符。 */
    @Size(max = 255, message = "图标地址过长")
    private String icon;
}
