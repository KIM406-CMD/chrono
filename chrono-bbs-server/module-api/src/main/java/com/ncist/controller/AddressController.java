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

/**
 * 收货地址控制器。
 *
 * <p>负责当前登录用户收货地址的查询、新增、修改、删除与默认地址设置。
 * 所有接口均以登录用户为数据边界，只能操作本人地址。接口前缀 {@code /api/address}。</p>
 */
@Tag(name = "收货地址", description = "收货地址的增删改查与默认地址设置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    /**
     * 查询当前登录用户的收货地址列表。
     *
     * <p>结果按默认地址优先排序，供下单时选择收货地址使用。</p>
     *
     * @return 当前用户的收货地址列表，可能为空
     */
    @Operation(summary = "地址列表（默认地址在前）")
    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.success(addressService.list(UserContext.getUserId()));
    }

    /**
     * 查询指定收货地址详情。
     *
     * @param id 地址 ID，必须属于当前登录用户
     * @return 地址详情
     */
    @Operation(summary = "地址详情")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @GetMapping("/{id}")
    public Result<AddressVO> detail(@PathVariable Long id) {
        return Result.success(addressService.detail(UserContext.getUserId(), id));
    }

    /**
     * 新增收货地址。
     *
     * @param dto 地址信息，包含收件人、联系电话、省市区与详细地址；
     *            字段由 Bean Validation 校验，{@code isDefault=1} 时同时设为默认地址
     * @return 新增地址的 ID
     */
    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AddressDTO dto) {
        return Result.success(addressService.create(UserContext.getUserId(), dto));
    }

    /**
     * 修改指定收货地址。
     *
     * @param id  地址 ID，必须属于当前登录用户
     * @param dto 新的地址信息，校验规则与新增一致
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "修改地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.update(UserContext.getUserId(), id, dto);
        return Result.success();
    }

    /**
     * 将指定地址设为默认收货地址。
     *
     * <p>同一用户至多一个默认地址，设置后原默认地址会被取消。</p>
     *
     * @param id 地址 ID，必须属于当前登录用户
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "设为默认地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.getUserId(), id);
        return Result.success();
    }

    /**
     * 删除指定收货地址。
     *
     * @param id 地址 ID，必须属于当前登录用户
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除地址")
    @Parameter(name = "id", description = "地址 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(UserContext.getUserId(), id);
        return Result.success();
    }
}
