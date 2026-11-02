package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.entity.Category;
import com.ncist.mapper.CategoryMapper;
import com.ncist.service.CategoryService;
import com.ncist.vo.CategoryTreeVO;
import com.ncist.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现：分类树使用 Redis 缓存（30 分钟），缓存故障时降级查库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 缓存 key（详见 detailed-design.md 第十五节）
    private static final String CATEGORY_TREE_KEY = "category:tree";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    // 查询三级分类树
    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 1. 先查缓存
        String cached = stringRedisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        if (cached != null) {
            List<CategoryTreeVO> tree = readTreeFromJson(cached);
            if (tree != null) {
                return tree;
            }
        }
        // 2. 缓存未命中，查库组装树
        List<Category> categories = categoryMapper.selectAll();
        List<CategoryTreeVO> tree = buildTree(categories);
        // 3. 写入缓存（30 分钟），失败只记日志不影响主流程
        writeTreeToCache(tree);
        return tree;
    }

    // 查询直接子分类
    @Override
    public List<CategoryVO> getChildren(Long parentId) {
        Long parent = parentId == null ? 0L : parentId;
        return categoryMapper.selectByParentId(parent).stream()
                .map(c -> CategoryVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .level(c.getLevel())
                        .sortOrder(c.getSortOrder())
                        .icon(c.getIcon())
                        .build())
                .collect(Collectors.toList());
    }

    // 内存组装三级树：parent_id = 0 为根节点
    private List<CategoryTreeVO> buildTree(List<Category> categories) {
        // 按父分类分组
        Map<Long, List<Category>> childrenMap = categories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() != 0)
                .collect(Collectors.groupingBy(Category::getParentId));
        // 递归构建根节点
        return categories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(c -> toTreeNode(c, childrenMap))
                .collect(Collectors.toList());
    }

    // 递归构建节点
    private CategoryTreeVO toTreeNode(Category category, Map<Long, List<Category>> childrenMap) {
        List<Category> children = childrenMap.getOrDefault(category.getId(), Collections.emptyList());
        return CategoryTreeVO.builder()
                .id(category.getId())
                .name(category.getName())
                .level(category.getLevel())
                .icon(category.getIcon())
                .children(children.stream()
                        .map(c -> toTreeNode(c, childrenMap))
                        .collect(Collectors.toList()))
                .build();
    }

    // 缓存 JSON 反序列化，失败返回 null 触发降级
    private List<CategoryTreeVO> readTreeFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<CategoryTreeVO>>() {
            });
        } catch (Exception e) {
            log.warn("分类树缓存反序列化失败，降级查库: {}", e.getMessage());
            return null;
        }
    }

    // 缓存 JSON 序列化，失败只记日志
    private void writeTreeToCache(List<CategoryTreeVO> tree) {
        try {
            stringRedisTemplate.opsForValue().set(CATEGORY_TREE_KEY, objectMapper.writeValueAsString(tree), CACHE_TTL);
        } catch (Exception e) {
            log.warn("分类树缓存写入失败: {}", e.getMessage());
        }
    }
}
