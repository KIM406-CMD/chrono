package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.CancelOrderDTO;
import com.ncist.dto.CreateOrderDTO;
import com.ncist.service.OrderService;
import com.ncist.vo.CreateOrderVO;
import com.ncist.vo.OrderDetailVO;
import com.ncist.vo.OrderListItemVO;
import com.ncist.vo.SubmitTokenVO;
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

@Tag(name = "订单管理", description = "订单的创建、支付、发货、收货、取消、退款等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "获取防重复提交 Token")
    @GetMapping("/submit-token")
    public Result<SubmitTokenVO> getSubmitToken() {
        return Result.success(orderService.getSubmitToken(UserContext.getUserId()));
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<CreateOrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(UserContext.getUserId(), dto));
    }

    @Operation(summary = "我的订单分页")
    @GetMapping("/list")
    public Result<PageResult<OrderListItemVO>> list(@RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return Result.success(orderService.listOrders(UserContext.getUserId(), status, page, size));
    }

    @Operation(summary = "订单详情")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(UserContext.getUserId(), id));
    }

    @Operation(summary = "取消订单（仅待支付）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) CancelOrderDTO dto) {
        orderService.cancelOrder(UserContext.getUserId(), id, dto == null ? null : dto.getReason());
        return Result.success();
    }

    @Operation(summary = "确认收货（仅已发货）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        orderService.receiveOrder(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "删除订单（仅已完成/已取消）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(UserContext.getUserId(), id);
        return Result.success();
    }
}
