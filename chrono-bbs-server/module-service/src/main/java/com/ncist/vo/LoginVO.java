package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册成功响应。
 *
 * <p>返回 JWT 与用户基本信息；前端需将 token 放入后续请求的 Authorization 头。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "登录/注册成功响应")
public class LoginVO {

    /** JWT 认证 Token。 */
    @Schema(description = "JWT 认证 Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    /** 用户基本信息（不含密码）。 */
    @Schema(description = "用户基本信息（不含密码）")
    private UserInfoVO userInfo;
}