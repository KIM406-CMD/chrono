package com.ncist.controller;

import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
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

@Tag(name = "管理后台", description = "用户管理、商家审核、分类管理、全平台商品/订单、全局销售报表（仅管理员）")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

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

    @Operation(summary = "启用/禁用用户（不能禁用管理员）")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "商家申请列表（status：0待审核/1已通过/2已拒绝）")
    @GetMapping("/merchants")
    public Result<PageResult<AdminMerchantVO>> merchants(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listMerchants(status, page, size));
    }

    @Operation(summary = "审核商家申请（approved：1通过/0拒绝，通过时申请人角色升级为商家）")
    @PutMapping("/merchants/{id}/review")
    public Result<Void> reviewMerchant(@PathVariable Long id, @Valid @RequestBody MerchantReviewDTO dto) {
        adminService.reviewMerchant(id, dto);
        return Result.success();
    }

    @Operation(summary = "新增分类（一级分类 parentId 传 0；层级必须与父分类连续）")
    @PostMapping("/categories")
    public Result<Long> createCategory(@Valid @RequestBody CategoryUpsertDTO dto) {
        return Result.success(adminService.createCategory(dto));
    }

    @Operation(summary = "修改分类（仅名称/排序/图标，层级与父级不可变更）")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpsertDTO dto) {
        adminService.updateCategory(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类（存在子分类或商品引用时拒绝）")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.success();
    }

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

    @Operation(summary = "全平台订单列表（分页，orderNo 精确 / status 筛选）")
    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> orders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.listOrders(orderNo, status, page, size));
    }

    @Operation(summary = "全局销售报表（groupBy：day/week/month）")
    @GetMapping("/report")
    public Result<AdminReportVO> report(
            @RequestParam(required = false) @Parameter(description = "开始日期 yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期 yyyy-MM-dd") String endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return Result.success(adminService.getReport(startDate, endDate, groupBy));
    }
}
