package com.ncist.controller;

import com.ncist.common.annotation.Public;
import com.ncist.common.result.Result;
import com.ncist.service.CategoryService;
import com.ncist.vo.CategoryTreeVO;
import com.ncist.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类查询控制器。
 *
 * <p>提供面向商城前端的公开分类查询能力，支持三级分类树与按父级查询子分类。
 * 所有接口均为公开访问。接口前缀 {@code /api/category}。</p>
 */
@Tag(name = "分类管理", description = "商品分类相关的接口，支持三级分类树的查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 查询完整的三级商品分类树。
     *
     * <p>公开接口，按层级与排序号组织为树形结构，供导航与筛选使用。</p>
     *
     * @return 三级分类树，根节点为一级分类
     */
    @Operation(summary = "获取完整三级分类树")
    @Public
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 查询指定父分类下的子分类。
     *
     * <p>公开接口，{@code parentId} 为 0 或不传时返回顶级分类。</p>
     *
     * @param parentId 父分类 ID，可为 {@code null}；为 0 或不传时查询一级分类
     * @return 子分类列表，按排序号升序
     */
    @Operation(summary = "获取子分类列表")
    @Parameter(name = "parentId", description = "父分类 ID，为 0 或不传时返回顶级分类", example = "0")
    @Public
    @GetMapping("/children")
    public Result<List<CategoryVO>> getChildren(@RequestParam(required = false) Long parentId) {
        return Result.success(categoryService.getChildren(parentId));
    }
}
