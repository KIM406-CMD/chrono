package com.ncist.config;

import com.ncist.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>注册 JWT 登录拦截器，并维护无需登录即可访问的接口白名单。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** JWT 登录拦截器。 */
    private final JwtInterceptor jwtInterceptor;

    /** 无需登录即可访问的路径白名单。 */
    private static final String[] WHITE_LIST = {
            "/api/user/captcha",
            "/api/user/register",
            "/api/user/login",
            "/api/user/forgot-code",
            "/api/user/reset-password",
            "/doc.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    /**
     * 注册 JWT 拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(WHITE_LIST);
    }
}