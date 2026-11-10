package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重置密码请求参数。
 *
 * <p>用户凭 /api/user/forgot-code 下发的验证码完成密码重置；验证码一次性使用。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "重置密码请求参数")
public class ResetPasswordDTO {

    /** 验证码 UUID，必填，来自 /api/user/forgot-code 返回。 */
    @Schema(description = "验证码 UUID（来自 /api/user/forgot-code 返回）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 UUID 不能为空")
    private String uuid;

    /** 短信/邮箱验证码，必填，校验通过后立即失效。 */
    @Schema(description = "短信/邮箱验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 新密码，必填，长度 6-20 位。 */
    @Schema(description = "新密码", example = "newpass123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 之间")
    private String newPassword;
}