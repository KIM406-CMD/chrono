package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡号支付请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPayDTO {

    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;

    @NotBlank(message = "卡号不能为空")
    @Pattern(regexp = "\\d{16,19}", message = "卡号必须为 16-19 位数字")
    private String cardNo;

    @NotBlank(message = "有效期不能为空")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "有效期格式必须为 MM/YY")
    private String expireDate;

    @NotBlank(message = "安全码不能为空")
    @Pattern(regexp = "\\d{3,4}", message = "安全码必须为 3-4 位数字")
    private String cvv;
}
