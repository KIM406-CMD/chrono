package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.service.RefundService;
import com.ncist.vo.RefundVO;
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

/**
 * 退款控制器。
 *
 * <p>提供买家退款/退货申请与查询、商家或管理员审核能力。
 * 所有接口要求登录；买家只能操作本人退款单，审核接口要求商家或管理员角色。
 * 接口前缀 {@code /api/refund}。</p>
 */
@Tag(name = "退款管理", description = "退款/退货申请、我的退款、退款审核")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refund")
public class RefundController {

    private final RefundService refundService;

    /**
     * 申请退款或退货。
     *
     * <p>订单状态须为已支付、已发货或已收货；每条订单明细仅可申请一次，
     * 退款金额需大于 0 且不超过该明细实付金额。申请后订单进入退款中状态。</p>
     *
     * @param dto 申请参数，包含订单明细 ID、类型（1-仅退款 2-退款退货）、
     *            原因与退款金额（单位：分）
     * @return 新增退款单 ID
     */
    @Operation(summary = "申请退款/退货")
    @PostMapping
    public Result<Long> apply(@Valid @RequestBody RefundCreateDTO dto) {
        return Result.success(refundService.apply(UserContext.getUserId(), dto));
    }

    /**
     * 分页查询当前用户的退款单。
     *
     * @param page 页码，从 1 开始，默认 1
     * @param size 每页条数，默认 10
     * @return 退款单分页结果
     */
    @Operation(summary = "退款列表（我的）")
    @GetMapping("/my")
    public Result<PageResult<RefundVO>> my(@RequestParam(required = false, defaultValue = "1") Integer page,
                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(refundService.myRefunds(UserContext.getUserId(), page, size));
    }

    /**
     * 查询退款单详情。
     *
     * <p>仅退款申请人本人可查看本人退款单。</p>
     *
     * @param id 退款单 ID
     * @return 退款单详情，包含退款金额、状态与审核意见
     */
    @Operation(summary = "退款详情")
    @Parameter(name = "id", description = "退款单 ID", example = "1")
    @GetMapping("/{id}")
    public Result<RefundVO> detail(@PathVariable Long id) {
        return Result.success(refundService.detail(UserContext.getUserId(), id));
    }

    /**
     * 审核退款或退货申请。
     *
     * <p>商家需资质有效且退款商品归属自己，管理员直接放行；每张退款单仅可审核一次。
     * 通过后订单置为已退款，拒绝后订单恢复为已收货。</p>
     *
     * @param id  退款单 ID
     * @param dto 审核参数，{@code approved=1} 同意、{@code approved=0} 拒绝，
     *            拒绝时建议填写审核原因
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "审核退款/退货（商家或管理员）")
    @Parameter(name = "id", description = "退款单 ID", example = "1")
    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody RefundReviewDTO dto) {
        refundService.review(UserContext.getUserId(), id, dto);
        return Result.success();
    }
}
