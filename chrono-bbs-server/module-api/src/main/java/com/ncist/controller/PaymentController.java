package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.Result;
import com.ncist.dto.CardPayDTO;
import com.ncist.dto.OrderIdDTO;
import com.ncist.service.PaymentService;
import com.ncist.vo.PaymentQrVO;
import com.ncist.vo.PayResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付控制器。
 *
 * <p>提供课程设计场景下的模拟支付能力，包括卡号支付与二维码支付。
 * 所有接口要求登录，且只能支付当前用户自己的待支付订单。
 * 接口前缀 {@code /api/payment}。</p>
 */
@Tag(name = "支付管理", description = "模拟支付的卡号支付与二维码支付接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 模拟卡号支付。
     *
     * <p>校验订单可支付性与卡号合法性后完成支付，不接入真实支付渠道。</p>
     *
     * @param dto 支付参数，包含订单 ID 与银行卡号、有效期、CVV
     * @return 支付结果，包含支付状态与支付时间
     */
    @Operation(summary = "卡号支付（模拟）")
    @PostMapping("/card")
    public Result<PayResultVO> payByCard(@Valid @RequestBody CardPayDTO dto) {
        return Result.success(paymentService.payByCard(UserContext.getUserId(), dto));
    }

    /**
     * 获取模拟支付二维码。
     *
     * <p>仅当前用户的待支付订单可生成二维码，二维码内容包含订单号与应付金额。</p>
     *
     * @param dto 参数包含订单 ID
     * @return 二维码信息，包含二维码地址与订单金额
     */
    @Operation(summary = "获取支付二维码（模拟）")
    @PostMapping("/qrcode")
    public Result<PaymentQrVO> qrcode(@Valid @RequestBody OrderIdDTO dto) {
        return Result.success(paymentService.getQrcode(UserContext.getUserId(), dto.getOrderId()));
    }

    /**
     * 确认二维码支付。
     *
     * <p>模拟扫码支付完成后的确认动作，将订单置为已支付并记录支付流水。</p>
     *
     * @param dto 参数包含订单 ID
     * @return 支付结果，包含支付状态与支付时间
     */
    @Operation(summary = "确认二维码支付（模拟）")
    @PostMapping("/confirm-qrcode")
    public Result<PayResultVO> confirmQrcode(@Valid @RequestBody OrderIdDTO dto) {
        return Result.success(paymentService.confirmQrcode(UserContext.getUserId(), dto.getOrderId()));
    }
}
