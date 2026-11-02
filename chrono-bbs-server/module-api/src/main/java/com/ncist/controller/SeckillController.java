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

@Tag(name = "秒杀券管理", description = "秒杀券的浏览、抢领、我的券、后台管理与下单优惠计算")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "秒杀券列表（公开）")
    @Parameter(name = "status", description = "1-进行中，0-未开始，不传查全部可抢券", example = "1")
    @Public
    @GetMapping("/list")
    public Result<PageResult<SeckillCouponVO>> listCoupons(@RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false, defaultValue = "1") Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(seckillService.listCoupons(status, page, size));
    }

    @Operation(summary = "秒杀券详情（公开）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @Public
    @GetMapping("/detail/{id}")
    public Result<SeckillCouponVO> getCouponDetail(@PathVariable Long id) {
        return Result.success(seckillService.getCouponDetail(id));
    }

    @Operation(summary = "抢领秒杀券")
    @Parameter(name = "couponId", description = "秒杀券 ID", example = "1")
    @PostMapping("/claim/{couponId}")
    public Result<SeckillClaimVO> claimCoupon(@PathVariable Long couponId) {
        return Result.success(seckillService.claimCoupon(couponId, UserContext.getUserId()));
    }

    @Operation(summary = "我的优惠券")
    @Parameter(name = "status", description = "0-未使用，1-已使用，2-已过期，不传查全部", example = "0")
    @GetMapping("/my-coupons")
    public Result<PageResult<MyCouponVO>> listMyCoupons(@RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(seckillService.listMyCoupons(UserContext.getUserId(), status, page, size));
    }

    @Operation(summary = "创建秒杀券（商家/管理员）")
    @PostMapping("/create")
    public Result<Long> createCoupon(@Valid @RequestBody SeckillCouponDTO dto) {
        return Result.success(seckillService.createCoupon(dto, UserContext.getRole()));
    }

    @Operation(summary = "编辑秒杀券（商家/管理员）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @PutMapping("/update/{id}")
    public Result<Void> updateCoupon(@PathVariable Long id, @Valid @RequestBody SeckillCouponDTO dto) {
        seckillService.updateCoupon(id, dto, UserContext.getRole());
        return Result.success();
    }

    @Operation(summary = "删除秒杀券（商家/管理员）")
    @Parameter(name = "id", description = "秒杀券 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        seckillService.deleteCoupon(id, UserContext.getRole());
        return Result.success();
    }

    @Operation(summary = "下单时计算优惠")
    @PostMapping("/calculate")
    public Result<SeckillCalculateVO> calculate(@Valid @RequestBody SeckillCalculateDTO dto) {
        return Result.success(seckillService.calculate(dto, UserContext.getUserId()));
    }
}
