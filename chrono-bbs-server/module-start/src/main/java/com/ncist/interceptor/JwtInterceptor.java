package com.ncist.interceptor;

import com.ncist.common.context.UserContext;
import com.ncist.common.enums.AuthErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
// JWT 拦截器
public class JwtInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String TOKEN_PREFIX = "token:";

    // 拦截器在请求处理前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 忽略 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 从请求头中提取 JWT
        String token = extractToken(request);
        // 解析 JWT 并获取用户信息
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));
        // JWT + Redis 双校验：token 必须与服务端记录一致（登出/改密/重置密码/禁用后立即失效）
        String serverToken = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
        if (serverToken == null) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
        }
        if (!serverToken.equals(token)) {
            throw new BusinessException(AuthErrorCode.TOKEN_INVALID);
        }
        // 从 JWT 中提取用户信息
        UserContext.UserInfo userInfo = new UserContext.UserInfo(
                userId,
                String.valueOf(claims.get("username")),
                Integer.valueOf(String.valueOf(claims.get("role")))
        );
        // 将用户信息存储到线程上下文
        UserContext.set(userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    // 从请求头中提取 JWT
    private String extractToken(HttpServletRequest request) {
        // 从请求头中提取 JWT
        String header = request.getHeader("Authorization");
        // 检查请求头是否包含 JWT
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer")) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }
        // 从请求头中提取 JWT 并返回
        return header.substring(7);
    }
}
