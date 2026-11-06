package com.ncist.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类新增/修改共用 DTO。
 * 新增：name、level 必填；parentId 一级分类传 0 或不传。
 * 修改：仅 name/sortOrder/icon 生效，parentId/level 不允许变更（防止打乱树结构）。
 */
@Data
public class CategoryUpsertDTO {

    @Size(max = 50, message = "分类名称最多 50 字")
    private String name;

    private Long parentId;

    private Integer level; // 1/2/3

    private Integer sortOrder;

    @Size(max = 255, message = "图标地址过长")
    private String icon;
}
