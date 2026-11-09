package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送重置密码验证码响应。
 *
 * <p>返回本次重置流程的 UUID 与脱敏账号；开发阶段额外返回验证码便于调试。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "发送重置密码验证码响应")
public class ForgotCodeVO {

    /** 本次验证码的唯一标识，重置密码时需携带。 */
    @Schema(description = "本次验证码的唯一标识，重置密码时需携带", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String uuid;

    /** 脱敏后的账号（手机号中间四位隐藏）。 */
    @Schema(description = "脱敏后的账号（手机号中间四位隐藏）", example = "138****8000")
    private String maskAccount;

    /** 验证码（模拟短信/邮件发送，仅开发阶段返回）。 */
    @Schema(description = "验证码（模拟短信/邮件发送，仅开发阶段返回）", example = "123456")
    private String code;
}