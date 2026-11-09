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

/**
 * 商家中心控制器。
 *
 * <p>面向商家角色，提供入驻申请、商家信息查询、商品与订单管理、发货及销售统计。
 * 入驻申请要求登录但允许普通用户提交；其余接口要求登录且角色为商家，
 * 数据范围限定为当前商家。接口前缀 {@code /api/merchant}。</p>
 */
@Tag(name = "商家管理", description = "商家入驻申请、信息查询、商品/订单管理、发货、销售统计")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 提交商家入驻申请。
     *
     * <p>普通用户登录后可提交，申请初始状态为待审核；同一用户仅可申请一次，
     * 已存在申请记录时拒绝重复提交。</p>
     *
     * @param dto 申请参数，包含店铺名称、营业执照与联系电话
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "商家入驻申请")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        merchantService.apply(UserContext.getUserId(), dto);
        return Result.success();
    }

    /**
     * 查询当前商家的店铺信息。
     *
     * @return 商家信息，包含店铺名称、资质、审核状态与驳回原因
     */
    @Operation(summary = "获取当前商家信息")
    @GetMapping("/info")
    public Result<MerchantInfoVO> info() {
        return Result.success(merchantService.getInfo(UserContext.getUserId()));
    }

    /**
     * 分页查询当前商家的商品。
     *
     * <p>要求商家资质审核通过，仅返回归属当前商家的商品。</p>
     *
     * @param status 商品状态筛选，取值 0-下架 1-上架，可为 {@code null} 表示全部
     * @param page   页码，从 1 开始，默认 1
     * @param size   每页条数，默认 10
     * @return 商品分页结果，每条为字段动态的 Map
     */
    @Operation(summary = "商家商品列表（分页）")
    @GetMapping("/products")
    public Result<PageResult<Map<String, Object>>> products(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(merchantService.listProducts(UserContext.getUserId(), status, page, size));
    }

    /**
     * 分页查询当前商家相关订单。
     *
     * @param status 订单状态筛选，可为 {@code null} 表示全部
     * @param page   页码，从 1 开始，默认 1
     * @param size   每页条数，默认 10
     * @return 商家订单分页结果
     */
    @Operation(summary = "商家订单列表（分页）")
    @GetMapping("/orders")
    public Result<PageResult<MerchantOrderVO>> orders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(merchantService.listOrders(UserContext.getUserId(), status, page, size));
    }

    /**
     * 商家发货。
     *
     * <p>仅可对已支付订单发货，且要求订单内全部商品均属于当前商家。</p>
     *
     * @param orderId 订单 ID
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "商家发货（仅已支付的订单，且订单商品全部属于该商家）")
    @Parameter(name = "orderId", description = "订单 ID", example = "1")
    @PutMapping("/ship/{orderId}")
    public Result<Void> ship(@PathVariable Long orderId) {
        merchantService.ship(UserContext.getUserId(), orderId);
        return Result.success();
    }

    /**
     * 查询当前商家的销售统计。
     *
     * @param startDate 统计开始日期，格式 {@code yyyy-MM-dd}，可为 {@code null}
     * @param endDate   统计结束日期，格式 {@code yyyy-MM-dd}，可为 {@code null}
     * @return 统计结果，包含热销商品、分类销售与销售趋势
     */
    @Operation(summary = "商家销售统计（topProducts / salesByCategory / salesTrend）")
    @GetMapping("/stats")
    public Result<MerchantStatsVO> stats(
            @RequestParam(required = false) @Parameter(description = "开始日期 yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期 yyyy-MM-dd") String endDate) {
        return Result.success(merchantService.getStats(UserContext.getUserId(), startDate, endDate));
    }
}
