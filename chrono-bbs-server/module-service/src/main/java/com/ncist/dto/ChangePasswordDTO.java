package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求参数。
 *
 * <p>登录用户校验旧密码后设置新密码；修改成功后当前登录态立即失效，需重新登录。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "修改密码请求参数")
public class ChangePasswordDTO {

    /** 旧密码，必填，用于校验当前登录用户身份。 */
    @Schema(description = "旧密码", example = "oldpass123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码，必填，长度 6-20 位。 */
    @Schema(description = "新密码", example = "newpass456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 之间")
    private String newPassword;
}