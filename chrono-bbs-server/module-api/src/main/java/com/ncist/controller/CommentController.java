package com.ncist.controller;

import com.ncist.common.annotation.Public;
import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.CommentAppendDTO;
import com.ncist.dto.CommentCreateDTO;
import com.ncist.dto.CommentReplyDTO;
import com.ncist.service.CommentService;
import com.ncist.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理", description = "商品评论、追评、回复、星级评分等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "商品评论列表（公开）")
    @Parameter(name = "productId", description = "商品 ID", example = "1")
    @Public
    @GetMapping("/list/{productId}")
    public Result<PageResult<CommentVO>> list(@PathVariable Long productId,
                                              @RequestParam(required = false) Integer star,
                                              @RequestParam(required = false, defaultValue = "1") Integer page,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(commentService.listByProduct(productId, star, page, size));
    }

    @Operation(summary = "发表评论（仅已收货订单明细）")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CommentCreateDTO dto) {
        return Result.success(commentService.create(UserContext.getUserId(), dto));
    }

    @Operation(summary = "追加评论（仅一级评论，评论者本人）")
    @PostMapping("/append")
    public Result<Long> append(@Valid @RequestBody CommentAppendDTO dto) {
        return Result.success(commentService.append(UserContext.getUserId(), dto));
    }

    @Operation(summary = "商家回复（仅自己商品的评论）")
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody CommentReplyDTO dto) {
        return Result.success(commentService.reply(UserContext.getUserId(), dto));
    }
}
