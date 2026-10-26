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
@Schema(description = "登录/注册成功响应")
public class LoginVO {

    @Schema(description = "JWT 认证 Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    @Schema(description = "用户基本信息（不含密码）")
    private UserInfoVO userInfo;
}