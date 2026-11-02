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

@Tag(name = "购物车", description = "购物车的增删改查、选中与全选")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.success(cartService.add(UserContext.getUserId(), dto));
    }

    @Operation(summary = "修改数量")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateQuantity(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "选中/取消选中（支持单个/批量/全部）")
    @PutMapping("/select")
    public Result<Void> select(@RequestBody CartSelectDTO dto) {
        cartService.select(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "全选/取消全选")
    @PutMapping("/select-all")
    public Result<Void> selectAll(@Valid @RequestBody SelectAllDTO dto) {
        cartService.selectAll(UserContext.getUserId(), dto.getSelected());
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @Parameter(name = "id", description = "购物车项 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.success();
    }
}
