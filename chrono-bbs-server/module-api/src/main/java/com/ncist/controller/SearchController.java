package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品搜索控制器。
 *
 * <p>用于承载 Elasticsearch 全文检索、搜索建议与搜索历史等 HTTP 接口；
 * 当前尚未提供对外方法。接口前缀 {@code /api/search}。</p>
 */
@Tag(name = "商品搜索", description = "全文检索、搜索建议、搜索历史等接口")
@RestController
@RequestMapping("/api/search")
public class SearchController {
}