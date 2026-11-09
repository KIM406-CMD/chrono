package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡号支付请求。
 *
 * <p>商城端用户选择卡号支付时提交，服务端校验卡面信息后完成订单支付。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPayDTO {

    /** 待支付订单 ID，必填，必须归属当前登录用户。 */
    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;

    /** 银行卡号，必填，16-19 位数字。 */
    @NotBlank(message = "卡号不能为空")
    @Pattern(regexp = "\\d{16,19}", message = "卡号必须为 16-19 位数字")
    private String cardNo;

    /** 银行卡有效期，必填，格式为 MM/YY（如 08/28）。 */
    @NotBlank(message = "有效期不能为空")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "有效期格式必须为 MM/YY")
    private String expireDate;

    /** 安全码（CVV），必填，3-4 位数字。 */
    @NotBlank(message = "安全码不能为空")
    @Pattern(regexp = "\\d{3,4}", message = "安全码必须为 3-4 位数字")
    private String cvv;
}
