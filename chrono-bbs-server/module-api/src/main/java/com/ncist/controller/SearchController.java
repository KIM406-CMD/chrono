package com.ncist.controller;

import com.ncist.common.annotation.Public;
import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.service.SearchService;
import com.ncist.vo.SearchItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "商品搜索", description = "全文检索、搜索建议、搜索历史等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "全文检索（keyword 与 categoryId 至少提供一个）")
    @Public
    @GetMapping
    public Result<PageResult<SearchItemVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        return Result.success(searchService.search(keyword, categoryId, brand, minPrice, maxPrice, sort,
                page, size, UserContext.getUserId()));
    }

    @Operation(summary = "搜索建议（自动补全）")
    @Public
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam(required = false) String keyword) {
        return Result.success(searchService.suggest(keyword));
    }

    @Operation(summary = "搜索历史（最近 20 条，仅登录用户）")
    @GetMapping("/history")
    public Result<List<String>> history() {
        return Result.success(searchService.history(UserContext.getUserId()));
    }

    @Operation(summary = "清除搜索历史（仅登录用户）")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        searchService.clearHistory(UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "热搜榜（近 7 天）")
    @Public
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.success(searchService.hotKeywords());
    }
}
