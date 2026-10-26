package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "发送重置密码验证码请求参数")
public class ForgotCodeDTO {

    @Schema(description = "手机号或邮箱", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;

    @Schema(description = "图形验证码 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 UUID 不能为空")
    private String captchaUuid;

    @Schema(description = "图形验证码输入值", example = "A3B7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}