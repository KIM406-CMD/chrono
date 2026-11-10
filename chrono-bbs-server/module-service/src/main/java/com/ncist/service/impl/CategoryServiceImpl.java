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
 * 分类服务实现。
 *
 * <p>分类树以 Redis 缓存（key {@code category:tree}，TTL 30 分钟）提升读取性能。
 * 缓存未命中或反序列化失败时降级查询数据库并重建缓存；
 * 缓存读写异常只记录告警日志，不影响主流程。直接子分类查询不走缓存。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    /** 分类数据访问组件。 */
    private final CategoryMapper categoryMapper;
    /** Redis 字符串操作模板，用于缓存分类树 JSON。 */
    private final StringRedisTemplate stringRedisTemplate;
    /** JSON 序列化组件，用于分类树缓存编解码。 */
    private final ObjectMapper objectMapper;

    /** 分类树缓存 key（详见 detailed-design.md 第十五节）。 */
    private static final String CATEGORY_TREE_KEY = "category:tree";
    /** 分类树缓存有效期：30 分钟。 */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * {@inheritDoc}
     *
     * <p>读取顺序为 Redis 缓存 → 数据库组装 → 回写缓存；缓存故障时直接使用数据库结果。</p>
     */
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

    /** {@inheritDoc} */
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

    /**
     * 在内存中组装三级分类树。
     *
     * <p>以 parent_id = 0（或 null）作为根节点，先按父分类分组，再递归构建子节点。</p>
     *
     * @param categories 全部分类记录
     * @return 分类树根节点列表
     */
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

    /**
     * 递归构建分类树节点。
     *
     * @param category    当前分类
     * @param childrenMap 按父分类 ID 分组的子分类映射
     * @return 当前分类对应的树节点
     */
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

    /**
     * 反序列化缓存中的分类树 JSON。
     *
     * @param json 缓存中的 JSON 字符串
     * @return 分类树列表；解析失败时返回 null 以触发降级查库
     */
    private List<CategoryTreeVO> readTreeFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<CategoryTreeVO>>() {
            });
        } catch (Exception e) {
            log.warn("分类树缓存反序列化失败，降级查库: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将分类树序列化后写入 Redis 缓存。
     *
     * <p>写入失败只记录告警日志，不影响查询结果返回。</p>
     *
     * @param tree 分类树列表
     */
    private void writeTreeToCache(List<CategoryTreeVO> tree) {
        try {
            stringRedisTemplate.opsForValue().set(CATEGORY_TREE_KEY, objectMapper.writeValueAsString(tree), CACHE_TTL);
        } catch (Exception e) {
            log.warn("分类树缓存写入失败: {}", e.getMessage());
        }
    }
}
