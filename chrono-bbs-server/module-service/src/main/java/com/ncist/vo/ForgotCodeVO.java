package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "发送重置密码验证码响应")
public class ForgotCodeVO {

    @Schema(description = "本次验证码的唯一标识，重置密码时需携带", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String uuid;

    @Schema(description = "脱敏后的账号（手机号中间四位隐藏）", example = "138****8000")
    private String maskAccount;
}