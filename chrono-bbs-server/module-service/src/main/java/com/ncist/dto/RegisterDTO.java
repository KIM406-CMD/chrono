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
     * 注册角色：1-普通用户（默认），2-商家；管理员角色不允许注册。
     *
     * <p>注册为商家时需同时填写店铺名称，注册后创建待审核的商家记录，
     * 审核通过后才能使用商家后台完整功能。</p>
     */
    @Schema(description = "注册角色：1-普通用户（默认），2-商家", example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer role;

    /**
     * 店铺名称，角色为商家（2）时必填。
     */
    @Schema(description = "店铺名称，商家注册时必填", example = "品质生活馆")
    @Size(max = 100, message = "店铺名称不能超过 100 个字符")
    private String shopName;

    /** 图形验证码 UUID，必填，由图形验证码接口下发。 */
    @Schema(description = "图形验证码 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 UUID 不能为空")
    private String captchaUuid;

    /** 图形验证码输入值，必填，校验后立即失效。 */
    @Schema(description = "图形验证码输入值", example = "A3B7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}