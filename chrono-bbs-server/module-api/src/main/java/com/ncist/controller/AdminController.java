package com.ncist.controller;

import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.service.AdminService;
import com.ncist.vo.AdminMerchantVO;
import com.ncist.vo.AdminReportVO;
import com.ncist.vo.AdminUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 管理后台控制器。
 *
 * <p>面向管理员角色，提供用户管理、商家入驻审核、商品分类维护、
 * 全平台商品/订单查询与全局销售报表。所有接口均要求登录且角色为管理员，
 * 越权访问会由业务层拒绝。接口前缀 {@code /api/admin}。</p>
 */
@Tag(name = "管理后台", description = "用户管理、商家审核、分类管理、全平台商品/订单、全局销售报表（仅管理员）")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 分页查询平台用户列表。
     *
     * @param keyword 搜索关键字，匹配用户名、昵称或手机号，可为 {@code null}
     * @param role    角色筛选，取值 1-普通用户 2-商家 3-管理员，可为 {@code null}
     * @param status  状态筛选，取值 0-禁用 1-正常，可为 {@code null}
     * @param page    页码，从 1 开始，默认 1
     * @param size    每页条数，默认 10
     * @return 用户分页结果
     */
    @Operation(summary = "用户列表（分页，keyword/role/status 筛选）")
    @GetMapping("/users")
    public Result<PageResult<AdminUserVO>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listUsers(keyword, role, status, page, size));
    }

    /**
     * 启用或禁用指定用户。
     *
     * <p>管理员账号不允许被禁用；禁用后该用户的登录 token 会立即失效。</p>
     *
     * @param id     用户 ID
     * @param status 目标状态，取值 0-禁用 1-正常
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "启用/禁用用户（不能禁用管理员）")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 分页查询商家入驻申请。
     *
     * @param status 审核状态筛选，取值 0-待审核 1-已通过 2-已拒绝，可为 {@code null} 表示全部
     * @param page   页码，从 1 开始，默认 1
     * @param size   每页条数，默认 10
     * @return 商家申请分页结果
     */
    @Operation(summary = "商家申请列表（status：0待审核/1已通过/2已拒绝）")
    @GetMapping("/merchants")
    public Result<PageResult<AdminMerchantVO>> merchants(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listMerchants(status, page, size));
    }

    /**
     * 审核商家入驻申请。
     *
     * <p>审核通过时申请人的角色升级为商家；拒绝时应填写原因。</p>
     *
     * @param id  商家申请 ID
     * @param dto 审核参数，{@code approved=1} 通过、{@code approved=0} 拒绝
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "审核商家申请（approved：1通过/0拒绝，通过时申请人角色升级为商家）")
    @PutMapping("/merchants/{id}/review")
    public Result<Void> reviewMerchant(@PathVariable Long id, @Valid @RequestBody MerchantReviewDTO dto) {
        adminService.reviewMerchant(id, dto);
        return Result.success();
    }

    /**
     * 新增商品分类。
     *
     * @param dto 分类信息，一级分类的 {@code parentId} 传 0；
     *            层级必须与父分类连续（父级为一级时只能新增二级，依此类推）
     * @return 新增分类的 ID
     */
    @Operation(summary = "新增分类（一级分类 parentId 传 0；层级必须与父分类连续）")
    @PostMapping("/categories")
    public Result<Long> createCategory(@Valid @RequestBody CategoryUpsertDTO dto) {
        return Result.success(adminService.createCategory(dto));
    }

    /**
     * 修改商品分类。
     *
     * @param id  分类 ID
     * @param dto 分类信息；仅名称、排序与图标可变更，层级和父级不可修改
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "修改分类（仅名称/排序/图标，层级与父级不可变更）")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpsertDTO dto) {
        adminService.updateCategory(id, dto);
        return Result.success();
    }

    /**
     * 删除商品分类。
     *
     * <p>存在子分类或已被商品引用时拒绝删除。</p>
     *
     * @param id 分类 ID
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除分类（存在子分类或商品引用时拒绝）")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 分页查询全平台商品。
     *
     * @param keyword    商品名称关键字，可为 {@code null}
     * @param categoryId 分类 ID 筛选，可为 {@code null}
     * @param merchantId 商家 ID 筛选，可为 {@code null}
     * @param status     商品状态筛选，取值 0-下架 1-上架，可为 {@code null}
     * @param page       页码，从 1 开始，默认 1
     * @param size       每页条数，默认 10
     * @return 商品分页结果，每条为字段动态的 Map
     */
    @Operation(summary = "全平台商品列表（分页，keyword/categoryId/merchantId/status 筛选）")
    @GetMapping("/products")
    public Result<PageResult<Map<String, Object>>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listProducts(keyword, categoryId, merchantId, status, page, size));
    }

    /**
     * 管理员修改任意商品。
     *
     * <p>可更新基础信息；SKU 列表非空时全量替换，不校验商品归属商家。</p>
     *
     * @param id  商品 ID
     * @param dto 商品更新参数
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "管理员修改商品（Mock 扩展）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        adminService.updateProduct(id, dto);
        return Result.success();
    }

    /**
     * 分页查询全平台订单。
     *
     * @param orderNo 订单号，精确匹配，可为 {@code null}
     * @param status  订单状态筛选，可为 {@code null}
     * @param page    页码，从 1 开始，默认 1
     * @param size    每页条数，默认 10
     * @return 订单分页结果，每条为字段动态的 Map
     */
    @Operation(summary = "全平台订单列表（分页，orderNo 精确 / status 筛选）")
    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> orders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listOrders(orderNo, status, page, size));
    }

    /**
     * 查询全局销售报表。
     *
     * @param startDate 统计开始日期，格式 {@code yyyy-MM-dd}，可为 {@code null}
     * @param endDate   统计结束日期，格式 {@code yyyy-MM-dd}，可为 {@code null}
     * @param groupBy   聚合维度，取值 {@code day}/{@code week}/{@code month}，默认 {@code day}
     * @return 销售报表，包含销售额、订单量等汇总数据
     */
    @Operation(summary = "全局销售报表（groupBy：day/week/month）")
    @GetMapping("/report")
    public Result<AdminReportVO> report(
            @RequestParam(required = false) @Parameter(description = "开始日期 yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期 yyyy-MM-dd") String endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return Result.success(adminService.getReport(startDate, endDate, groupBy));
    }
}
