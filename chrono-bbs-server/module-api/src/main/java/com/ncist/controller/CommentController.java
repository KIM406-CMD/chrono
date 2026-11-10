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

/**
 * 商品评论控制器。
 *
 * <p>提供商品评论列表查询、发表评论、追加评论与商家回复能力。
 * 评论列表为公开接口，其余接口要求登录，并校验订单归属或商品归属。
 * 接口前缀 {@code /api/comment}。</p>
 */
@Tag(name = "评论管理", description = "商品评论、追评、回复、星级评分等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询指定商品的评论列表。
     *
     * <p>公开接口，可按星级筛选，供商品详情页展示。</p>
     *
     * @param productId 商品 ID
     * @param star      星级筛选，取值 1-5，可为 {@code null} 表示不限
     * @param page      页码，从 1 开始，默认 1
     * @param size      每页条数，默认 10
     * @return 评论分页结果
     */
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

    /**
     * 发表商品评论。
     *
     * <p>仅允许对当前用户已收货订单中的订单明细发表评论，且每条明细仅可评论一次。</p>
     *
     * @param dto 评论参数，包含商品 ID、订单明细 ID、评论内容与 1-5 星评分
     * @return 新增评论 ID
     */
    @Operation(summary = "发表评论（仅已收货订单明细）")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CommentCreateDTO dto) {
        return Result.success(commentService.create(UserContext.getUserId(), dto));
    }

    /**
     * 追加评论（追评）。
     *
     * <p>仅评论者本人可对一级评论追加，且每条一级评论仅可追加一次。</p>
     *
     * @param dto 追评参数，包含一级评论 ID 与追加内容
     * @return 新增追评 ID
     */
    @Operation(summary = "追加评论（仅一级评论，评论者本人）")
    @PostMapping("/append")
    public Result<Long> append(@Valid @RequestBody CommentAppendDTO dto) {
        return Result.success(commentService.append(UserContext.getUserId(), dto));
    }

    /**
     * 商家回复评论。
     *
     * <p>仅商家可操作，且只能回复自己商品的评论。</p>
     *
     * @param dto 回复参数，包含被回复的评论 ID 与回复内容
     * @return 新增回复 ID
     */
    @Operation(summary = "商家回复（仅自己商品的评论）")
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody CommentReplyDTO dto) {
        return Result.success(commentService.reply(UserContext.getUserId(), dto));
    }
}
