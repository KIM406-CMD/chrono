package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户信息响应")
public class UserInfoVO {

    @Schema(description = "用户 ID", example = "2")
    private Long id;

    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "昵称", example = "测试用户")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "角色：1=普通用户，2=商家，3=管理员", example = "1")
    private Integer role;

    @Schema(description = "状态：0=禁用，1=正常", example = "1")
    private Integer status;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
}