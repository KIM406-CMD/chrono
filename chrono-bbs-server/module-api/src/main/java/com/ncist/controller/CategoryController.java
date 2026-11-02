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

@Tag(name = "分类管理", description = "商品分类相关的接口，支持三级分类树的查询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取完整三级分类树")
    @Public
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取子分类列表")
    @Parameter(name = "parentId", description = "父分类 ID，为 0 或不传时返回顶级分类", example = "0")
    @Public
    @GetMapping("/children")
    public Result<List<CategoryVO>> getChildren(@RequestParam(required = false) Long parentId) {
        return Result.success(categoryService.getChildren(parentId));
    }
}
