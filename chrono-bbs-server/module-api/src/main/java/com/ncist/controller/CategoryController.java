package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "分类管理", description = "商品分类相关的接口，支持三级分类树的查询")
@RestController
@RequestMapping("/api/category")
public class CategoryController {
}