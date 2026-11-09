package com.ncist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改个人信息请求参数。
 *
 * <p>登录用户更新昵称、手机号、邮箱或头像时使用；所有字段均为可选，
 * 传 null 或空字符串表示不修改该字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "修改个人信息请求参数")
public class UpdateProfileDTO {

    /** 昵称，可选；为空时保持原值。 */
    @Schema(description = "昵称", example = "新昵称")
    private String nickname;

    /** 手机号，可选，填写时须为 11 位手机号；为空时保持原值。 */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    /** 邮箱，可选，填写时须符合邮箱格式；为空时保持原值。 */
    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 头像 URL，可选，需先通过上传接口获取；为空时保持原值。 */
    @Schema(description = "头像 URL（先上传获取）")
    private String avatar;
}