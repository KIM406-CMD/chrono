package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.Result;
import com.ncist.dto.CartAddDTO;
import com.ncist.dto.CartSelectDTO;
import com.ncist.dto.CartUpdateDTO;
import com.ncist.dto.SelectAllDTO;
import com.ncist.service.CartService;
import com.ncist.vo.CartItemVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车控制器。
 *
 * <p>维护当前登录用户的购物车项，支持添加、修改数量、选中、全选、删除与清空。
 * 所有接口均要求登录，并以登录用户为数据边界，禁止操作他人购物车。
 * 接口前缀 {@code /api/cart}。</p>
 */
@Tag(name = "购物车", description = "购物车的增删改查、选中与全选")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    /**
     * 查询当前用户的购物车列表。
     *
     * @return 购物车项列表，包含商品快照、数量与选中状态
     */
    @Operation(summary = "获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    /**
     * 将商品规格加入购物车。
     *
     * <p>商品必须处于上架状态；同一 SKU 重复加入时累加数量，
     * 累加后的数量不得超过该 SKU 库存。</p>
     *
     * @param dto 加入参数，包含 SKU ID、数量与商品 ID，数量需为正数
     * @return 购物车项 ID（已存在时为原购物车项 ID）
     */
    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.success(cartService.add(UserContext.getUserId(), dto));
    }

    /**
     * 修改购物车项数量。
     *
     * @param dto 参数包含购物车项 ID 与目标数量，数量需为正数且不超过 SKU 库存
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "修改数量")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateQuantity(UserContext.getUserId(), dto);
        return Result.success();
    }

    /**
     * 选中或取消选中购物车项。
     *
     * <p>支持单个（{@code cartItemId}）、批量（{@code cartItemIds}）操作；
     * 两者均未指定时作用于当前用户全部购物车项。选中状态取值 1/0 或 true/false。</p>
     *
     * @param dto 选中参数，包含目标购物车项与选中状态
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "选中/取消选中（支持单个/批量/全部）")
    @PutMapping("/select")
    public Result<Void> select(@RequestBody CartSelectDTO dto) {
        cartService.select(UserContext.getUserId(), dto);
        return Result.success();
    }

    /**
     * 全选或取消全选购物车。
     *
     * @param dto 参数 {@code selected} 取值 1-全选 0-取消全选
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "全选/取消全选")
    @PutMapping("/select-all")
    public Result<Void> selectAll(@Valid @RequestBody SelectAllDTO dto) {
        cartService.selectAll(UserContext.getUserId(), dto.getSelected());
        return Result.success();
    }

    /**
     * 删除指定购物车项。
     *
     * @param id 购物车项 ID，必须属于当前登录用户
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除购物车项")
    @Parameter(name = "id", description = "购物车项 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(UserContext.getUserId(), id);
        return Result.success();
    }

    /**
     * 清空当前用户的购物车。
     *
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.success();
    }
}
