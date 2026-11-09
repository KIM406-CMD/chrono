package com.ncist.service;

import com.ncist.vo.CategoryTreeVO;
import com.ncist.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类服务。
 *
 * <p>提供分类树与直接子分类查询，供前台导航与商品筛选使用。
 * 三级分类树使用 Redis 缓存 30 分钟；缓存未命中或反序列化失败时
 * 自动降级查询数据库并重建缓存，缓存故障不影响查询结果。</p>
 */
public interface CategoryService {

    /**
     * 查询三级分类树（根节点为一级分类，逐级嵌套子分类）。
     *
     * <p>结果优先读取 Redis 缓存（key 为 {@code category:tree}，TTL 30 分钟），
     * 缓存未命中时查库组装并回写缓存。</p>
     *
     * @return 分类树列表；无分类时返回空列表
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 查询指定父分类下的直接子分类。
     *
     * @param parentId 父分类 ID；为空或 0 时查询顶级分类
     * @return 直接子分类列表；无子分类时返回空列表
     */
    List<CategoryVO> getChildren(Long parentId);
}
