package com.ncist.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.entity.Category;
import com.ncist.mapper.CategoryMapper;
import com.ncist.service.impl.CategoryServiceImpl;
import com.ncist.vo.CategoryTreeVO;
import com.ncist.vo.CategoryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CategoryServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分类服务单元测试")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // 三级分类测试数据：食品(1) -> 水果(11) -> 苹果(111)
    private Category category(long id, String name, long parentId, int level) {
        return Category.builder()
                .id(id).name(name).parentId(parentId).level(level).sortOrder(0).icon(null)
                .build();
    }

    @Test
    @DisplayName("获取分类树-缓存命中时不查库")
    void getCategoryTreeCacheHit() {
        String cached = "[{\"id\":1,\"name\":\"食品\",\"level\":1,\"children\":[]}]";
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:tree")).thenReturn(cached);

        List<CategoryTreeVO> tree = categoryService.getCategoryTree();

        assertEquals(1, tree.size());
        assertEquals("食品", tree.get(0).getName());
        verify(categoryMapper, org.mockito.Mockito.never()).selectAll();
    }

    @Test
    @DisplayName("获取分类树-缓存未命中时查库并组装三级树")
    void getCategoryTreeCacheMissBuild() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:tree")).thenReturn(null);
        when(categoryMapper.selectAll()).thenReturn(List.of(
                category(1, "食品", 0, 1),
                category(2, "数码", 0, 1),
                category(11, "水果", 1, 2),
                category(111, "苹果", 11, 3)
        ));

        List<CategoryTreeVO> tree = categoryService.getCategoryTree();

        assertEquals(2, tree.size());
        CategoryTreeVO food = tree.stream().filter(t -> t.getId() == 1).findFirst().orElse(null);
        assertNotNull(food);
        assertEquals(1, food.getChildren().size());
        assertEquals("水果", food.getChildren().get(0).getName());
        assertEquals("苹果", food.getChildren().get(0).getChildren().get(0).getName());
        // 验证写缓存
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq("category:tree"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(java.time.Duration.class));
    }

    @Test
    @DisplayName("获取子分类-parentId 为空时默认查顶级分类")
    void getChildrenDefaultRoot() {
        when(categoryMapper.selectByParentId(0L)).thenReturn(List.of(category(1, "食品", 0, 1)));

        List<CategoryVO> children = categoryService.getChildren(null);

        assertEquals(1, children.size());
        assertEquals("食品", children.get(0).getName());
        assertEquals(1, children.get(0).getLevel());
    }

    @Test
    @DisplayName("获取分类树-缓存损坏时降级查库")
    void getCategoryTreeCorruptedCacheFallback() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:tree")).thenReturn("not-a-json{{{");
        when(categoryMapper.selectAll()).thenReturn(List.of(category(1, "食品", 0, 1)));

        List<CategoryTreeVO> tree = categoryService.getCategoryTree();

        assertEquals(1, tree.size());
        verify(categoryMapper).selectAll();
    }
}
