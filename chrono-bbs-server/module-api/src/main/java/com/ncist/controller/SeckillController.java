package com.ncist.controller;

import com.ncist.common.annotation.Public;
import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.dto.SeckillCouponDTO;
import com.ncist.service.SeckillService;
import com.ncist.vo.MyCouponVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SeckillClaimVO;
import com.ncist.vo.SeckillCouponVO;
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
 * 秒杀券控制器。
 *
 * <p>提供秒杀券的公开浏览、用户抢领与我的优惠券查询，以及商家/管理员的券维护能力，
 * 并支持下单前的优惠金额试算。接口前缀 {@code /api/seckill}。</p>
 */
@Tag(name = "秒杀券管理", description = "秒杀券的浏览、抢领、我的券、后台管理与下单优惠计算")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    /**
     * 分页查询可抢领的秒杀券。
     *
     * <p>公开接口，可按活动状态筛选。</p>
     *
     * @param status 状态筛选，取值 1-进行中 0-未开始，可为 {@code null} 表示全部
     * @param page   页码，从 1 开始，默认 1
     * @param size   每页条数，默认 10
     * @return 秒杀券分页结果
     */
    @Operation(summary = "秒杀券列表（公开）")
    @Parameter(name = "status", description = "1-进行中，0-未开始，不传查全部可抢券", example = "1")
    @Public
    @GetMapping("/list")
    public Result<PageResult<SeckillCouponVO>> listCoupons(@RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false, defaultValue = "1") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(seckillService.listCoupons(status, page, size, UserContext.getUserId()));
    }

    /**
     * 查询秒杀券详情。
     *
     * <p>公开接口，返回券面额、使用门槛、有效期与剩余数量等；
     * 携带有效登录令牌时同时回填当前用户的领取状态。</p>
     *
     * @param id 秒杀券 ID
     * @return 秒杀券详情
     */
    @Operation(summary = "秒杀券详情（公开）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @Public
    @GetMapping("/detail/{id}")
    public Result<SeckillCouponVO> getCouponDetail(@PathVariable Long id) {
        return Result.success(seckillService.getCouponDetail(id, UserContext.getUserId()));
    }

    /**
     * 抢领秒杀券。
     *
     * <p>要求登录；校验活动进行中、库存充足且未超过每人限领数量，
     * 抢领成功后生成一张未使用的用户优惠券。</p>
     *
     * @param couponId 秒杀券 ID
     * @return 抢领结果，包含用户券 ID 与券面信息
     */
    @Operation(summary = "抢领秒杀券")
    @Parameter(name = "couponId", description = "秒杀券 ID", example = "1")
    @PostMapping("/claim/{couponId}")
    public Result<SeckillClaimVO> claimCoupon(@PathVariable Long couponId) {
        return Result.success(seckillService.claimCoupon(couponId, UserContext.getUserId()));
    }

    /**
     * 分页查询当前用户的优惠券。
     *
     * @param status 使用状态筛选，取值 0-未使用 1-已使用 2-已过期，可为 {@code null} 表示全部
     * @param page   页码，从 1 开始，默认 1
     * @param size   每页条数，默认 10
     * @return 我的优惠券分页结果
     */
    @Operation(summary = "我的优惠券")
    @Parameter(name = "status", description = "0-未使用，1-已使用，2-已过期，不传查全部", example = "0")
    @GetMapping("/my-coupons")
    public Result<PageResult<MyCouponVO>> listMyCoupons(@RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(seckillService.listMyCoupons(UserContext.getUserId(), status, page, size));
    }

    /**
     * 创建秒杀券。
     *
     * <p>要求登录且角色为商家或管理员；结束时间必须晚于开始时间，
     * 优惠类型与优惠值需合法，活动状态按时间窗自动推导。</p>
     *
     * @param dto 券参数，包含名称、优惠类型与面额、使用门槛、发放总量、
     *            有效期（{@code yyyy-MM-dd HH:mm:ss}）与每人限领数量
     * @return 新增秒杀券 ID
     */
    @Operation(summary = "创建秒杀券（商家/管理员）")
    @PostMapping("/create")
    public Result<Long> createCoupon(@Valid @RequestBody SeckillCouponDTO dto) {
        return Result.success(seckillService.createCoupon(dto, UserContext.getRole()));
    }

    /**
     * 编辑秒杀券。
     *
     * <p>要求登录且角色为商家或管理员；修改发放总量时同步调整剩余数量，
     * 并根据新的时间窗重新计算活动状态。</p>
     *
     * @param id  秒杀券 ID
     * @param dto 券参数，结构同创建
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "编辑秒杀券（商家/管理员）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @PutMapping("/update/{id}")
    public Result<Void> updateCoupon(@PathVariable Long id, @Valid @RequestBody SeckillCouponDTO dto) {
        seckillService.updateCoupon(id, dto, UserContext.getRole());
        return Result.success();
    }

    /**
     * 删除秒杀券。
     *
     * <p>要求登录且角色为商家或管理员；仅未开始（无人可领取）的券可删除。</p>
     *
     * @param id 秒杀券 ID
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除秒杀券（商家/管理员）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        seckillService.deleteCoupon(id, UserContext.getRole());
        return Result.success();
    }

    /**
     * 下单前计算秒杀券优惠。
     *
     * <p>校验用户券归属、未使用状态与使用门槛后试算抵扣金额，不产生实际扣减。</p>
     *
     * @param dto 计算参数，包含用户优惠券 ID（可为空表示不使用券）与订单原始总金额（单位：分）
     * @return 优惠计算结果，包含原价、抵扣金额与应付金额
     */
    @Operation(summary = "下单时计算优惠")
    @PostMapping("/calculate")
    public Result<SeckillCalculateVO> calculate(@Valid @RequestBody SeckillCalculateDTO dto) {
        return Result.success(seckillService.calculate(dto, UserContext.getUserId()));
    }
}