package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台-用户列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
}
