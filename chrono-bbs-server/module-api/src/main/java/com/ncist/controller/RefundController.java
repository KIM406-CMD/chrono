package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.service.RefundService;
import com.ncist.vo.RefundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "退款管理", description = "退款/退货申请、我的退款、退款审核")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refund")
public class RefundController {

    private final RefundService refundService;

    @Operation(summary = "申请退款/退货")
    @PostMapping
    public Result<Long> apply(@Valid @RequestBody RefundCreateDTO dto) {
        return Result.success(refundService.apply(UserContext.getUserId(), dto));
    }

    @Operation(summary = "退款列表（我的）")
    @GetMapping("/my")
    public Result<PageResult<RefundVO>> my(@RequestParam(required = false, defaultValue = "1") Integer page,
                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(refundService.myRefunds(UserContext.getUserId(), page, size));
    }

    @Operation(summary = "退款详情")
    @Parameter(name = "id", description = "退款单 ID", example = "1")
    @GetMapping("/{id}")
    public Result<RefundVO> detail(@PathVariable Long id) {
        return Result.success(refundService.detail(UserContext.getUserId(), id));
    }

    @Operation(summary = "审核退款/退货（商家或管理员）")
    @Parameter(name = "id", description = "退款单 ID", example = "1")
    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody RefundReviewDTO dto) {
        refundService.review(UserContext.getUserId(), id, dto);
        return Result.success();
    }
}
