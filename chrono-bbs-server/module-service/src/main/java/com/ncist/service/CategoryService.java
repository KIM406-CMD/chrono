package com.ncist.service;

import com.ncist.vo.CategoryTreeVO;
import com.ncist.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务
 */
public interface CategoryService {

    // 查询三级分类树（Redis 缓存 30 分钟）
    List<CategoryTreeVO> getCategoryTree();

    // 查询指定父分类下的直接子分类（parentId 为空时查顶级分类）
    List<CategoryVO> getChildren(Long parentId);
}
