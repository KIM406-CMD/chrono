package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品管理", description = "商品的增删改查、上下架、浏览等接口")
@RestController
@RequestMapping("/api/product")
public class ProductController {
}