package com.ncist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码配置。
 *
 * <p>统一提供 BCrypt 密码编码器，用于注册、登录与改密流程。</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * 创建 BCrypt 密码编码器。
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}