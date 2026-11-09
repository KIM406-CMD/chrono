package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求参数。
 *
 * <p>注册时先校验图形验证码，再校验用户名、手机号唯一性；注册成功直接返回登录态。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    /** 用户名，必填，3-20 位，需全局唯一。 */
    @Schema(description = "用户名", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度需在 3-20 之间")
    private String username;

    /** 密码，必填，6-20 位。 */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 之间")
    private String password;

    /** 手机号，必填，11 位，需全局唯一。 */
    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    /** 邮箱，可选，填写时需符合邮箱格式。 */
    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 角色字段，保留用于兼容旧客户端。
     *
     * <p>注册固定创建普通用户（role=1），该字段不再生效；商家角色只能通过
     * 入驻申请并由管理员审核通过后升级。</p>
     */
    @Schema(description = "保留字段，注册固定为普通用户（1）", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer role;

    /** 图形验证码 UUID，必填，由图形验证码接口下发。 */
    @Schema(description = "图形验证码 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 UUID 不能为空")
    private String captchaUuid;

    /** 图形验证码输入值，必填，校验后立即失效。 */
    @Schema(description = "图形验证码输入值", example = "A3B7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}