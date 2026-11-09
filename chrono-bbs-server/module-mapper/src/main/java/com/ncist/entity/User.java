package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户账号实体，对应 {@code user} 表。
 *
 * <p>买家、商家、管理员共用该表，通过 {@code role} 区分；商家额外在
 * {@code merchant} 表保存入驻与审核信息。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    /** 登录用户名，全局唯一（唯一索引 uk_username）。 */
    private String username;
    /** BCrypt 加密后的密码摘要，禁止明文存储。 */
    private String password;
    /** 昵称，用于前端展示。 */
    private String nickname;
    /** 手机号，可用于登录与找回密码。 */
    private String phone;
    /** 邮箱，可用于登录与找回密码。 */
    private String email;
    /** 头像 URL。 */
    private String avatar;
    /** 角色：0-游客（未登录），1-普通用户，2-商家，3-管理员；与 UserRoleEnum 保持一致。 */
    private Integer role;
    /** 账号状态：1-正常，0-禁用；禁用后登录态立即失效。 */
    private Integer status;
    /** 注册时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}