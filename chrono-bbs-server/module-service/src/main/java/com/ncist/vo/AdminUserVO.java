package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台-用户列表项。
 *
 * <p>管理员分页查看与筛选平台用户时使用。</p>
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
    /** 角色：1=普通用户，2=商家，3=管理员。 */
    private Integer role;
    /** 账号状态：0=禁用，1=正常。 */
    private Integer status;
    private LocalDateTime createdAt;
}
