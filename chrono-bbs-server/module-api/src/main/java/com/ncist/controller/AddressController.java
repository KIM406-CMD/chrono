package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.Result;
import com.ncist.dto.AddressDTO;
import com.ncist.service.AddressService;
import com.ncist.vo.AddressVO;
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

@Tag(name = "收货地址", description = "收货地址的增删改查与默认地址设置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "地址列表（默认地址在前）")
    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.success(addressService.list(UserContext.getUserId()));
    }

    @Operation(summary = "地址详情")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @GetMapping("/{id}")
    public Result<AddressVO> detail(@PathVariable Long id) {
        return Result.success(addressService.detail(UserContext.getUserId(), id));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AddressDTO dto) {
        return Result.success(addressService.create(UserContext.getUserId(), dto));
    }

    @Operation(summary = "修改地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.update(UserContext.getUserId(), id, dto);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(UserContext.getUserId(), id);
        return Result.success();
    }
}
