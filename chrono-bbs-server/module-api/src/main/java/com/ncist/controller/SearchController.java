package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品搜索", description = "全文检索、搜索建议、搜索历史等接口")
@RestController
@RequestMapping("/api/search")
public class SearchController {
}