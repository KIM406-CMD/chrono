package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.service.MerchantService;
import com.ncist.vo.MerchantInfoVO;
import com.ncist.vo.MerchantOrderVO;
import com.ncist.vo.MerchantStatsVO;
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

import java.util.Map;

@Tag(name = "商家管理", description = "商家入驻申请、信息查询、商品/订单管理、发货、销售统计")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "商家入驻申请")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        merchantService.apply(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "获取当前商家信息")
    @GetMapping("/info")
    public Result<MerchantInfoVO> info() {
        return Result.success(merchantService.getInfo(UserContext.getUserId()));
    }

    @Operation(summary = "商家商品列表（分页）")
    @GetMapping("/products")
    public Result<PageResult<Map<String, Object>>> products(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(merchantService.listProducts(UserContext.getUserId(), status, page, size));
    }

    @Operation(summary = "商家订单列表（分页）")
    @GetMapping("/orders")
    public Result<PageResult<MerchantOrderVO>> orders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(merchantService.listOrders(UserContext.getUserId(), status, page, size));
    }

    @Operation(summary = "商家发货（仅已支付的订单，且订单商品全部属于该商家）")
    @Parameter(name = "orderId", description = "订单 ID", example = "1")
    @PutMapping("/ship/{orderId}")
    public Result<Void> ship(@PathVariable Long orderId) {
        merchantService.ship(UserContext.getUserId(), orderId);
        return Result.success();
    }

    @Operation(summary = "商家销售统计（topProducts / salesByCategory / salesTrend）")
    @GetMapping("/stats")
    public Result<MerchantStatsVO> stats(
            @RequestParam(required = false) @Parameter(description = "开始日期 yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期 yyyy-MM-dd") String endDate) {
        return Result.success(merchantService.getStats(UserContext.getUserId(), startDate, endDate));
    }
}
