package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求参数。
 *
 * <p>登录时先校验图形验证码，再校验用户名、密码与账号状态；登录成功返回 JWT。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户登录请求参数")
public class LoginDTO {

    /** 用户名，必填；当前仅支持用户名登录。 */
    @Schema(description = "用户名", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码，必填。 */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 图形验证码 UUID，必填，由图形验证码接口下发。 */
    @Schema(description = "图形验证码 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 UUID 不能为空")
    private String captchaUuid;

    /** 图形验证码输入值，必填，校验后立即失效。 */
    @Schema(description = "图形验证码输入值", example = "A3B7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}