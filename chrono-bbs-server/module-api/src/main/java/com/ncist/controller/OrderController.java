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

/**
 * 订单控制器。
 *
 * <p>面向买家，提供订单创建、列表与详情查询、取消、确认收货与删除。
 * 所有接口要求登录，并校验订单归属当前用户。接口前缀 {@code /api/order}。</p>
 */
@Tag(name = "订单管理", description = "订单的创建、支付、发货、收货、取消、退款等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取防重复提交 Token。
     *
     * <p>Token 与当前用户绑定并在 5 分钟内有效，创建订单时一次性消费，
     * 用于防止重复下单。</p>
     *
     * @return 提交令牌，包含 {@code token} 字符串
     */
    @Operation(summary = "获取防重复提交 Token")
    @GetMapping("/submit-token")
    public Result<SubmitTokenVO> getSubmitToken() {
        return Result.success(orderService.getSubmitToken(UserContext.getUserId()));
    }

    /**
     * 创建订单。
     *
     * <p>从购物车选定明细生成订单，依次校验一次性提交 Token、收货地址归属、
     * 商品上架状态与库存，并以原子扣减方式锁定库存。</p>
     *
     * @param dto 下单参数，包含提交 Token、收货地址 ID、购物车项 ID 列表、
     *            备注与可选优惠券 ID
     * @return 创建结果，包含订单号与应付金额
     */
    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<CreateOrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(UserContext.getUserId(), dto));
    }

    /**
     * 分页查询当前用户的订单。
     *
     * @param status 订单状态筛选，可为 {@code null} 表示全部
     * @param page   页码，可为 {@code null}，服务端按从 1 开始处理
     * @param size   每页条数，可为 {@code null}，服务端默认为 10
     * @return 订单分页结果
     */
    @Operation(summary = "我的订单分页")
    @GetMapping("/list")
    public Result<PageResult<OrderListItemVO>> list(@RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return Result.success(orderService.listOrders(UserContext.getUserId(), status, page, size));
    }

    /**
     * 查询指定订单详情。
     *
     * @param id 订单 ID，必须属于当前登录用户
     * @return 订单详情，包含订单项、金额与状态信息
     */
    @Operation(summary = "订单详情")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(UserContext.getUserId(), id));
    }

    /**
     * 取消指定订单。
     *
     * <p>仅待支付订单可取消；取消后回补商品库存并将已使用的优惠券恢复为未使用。</p>
     *
     * @param id  订单 ID，必须属于当前登录用户
     * @param dto 取消参数，可携带取消原因，允许为 {@code null}
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "取消订单（仅待支付）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) CancelOrderDTO dto) {
        orderService.cancelOrder(UserContext.getUserId(), id, dto == null ? null : dto.getReason());
        return Result.success();
    }

    /**
     * 确认收货。
     *
     * <p>仅已发货订单可确认收货，确认后订单状态变为已完成。</p>
     *
     * @param id 订单 ID，必须属于当前登录用户
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "确认收货（仅已发货）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        orderService.receiveOrder(UserContext.getUserId(), id);
        return Result.success();
    }

    /**
     * 删除指定订单。
     *
     * <p>仅已完成或已取消的订单可删除。</p>
     *
     * @param id 订单 ID，必须属于当前登录用户
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除订单（仅已完成/已取消）")
    @Parameter(name = "id", description = "订单 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(UserContext.getUserId(), id);
        return Result.success();
    }
}
