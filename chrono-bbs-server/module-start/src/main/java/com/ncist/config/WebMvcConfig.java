package com.ncist.config;

import com.ncist.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    /** 图片上传配置，用于本地存储模式的静态资源映射。 */
    private final UploadProperties uploadProperties;

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

    /**
     * 本地图片上传模式下，将 {@code upload.public-prefix} 映射到本地目录。
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!uploadProperties.isLocalMode()) {
            return;
        }
        Path dir = Paths.get(uploadProperties.getLocalDir()).toAbsolutePath().normalize();
        String prefix = uploadProperties.getPublicPrefix() == null
                ? "/uploads" : uploadProperties.getPublicPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        registry.addResourceHandler(prefix + "/**")
                .addResourceLocations(dir.toUri().toString());
    }
}