package com.ncist.interceptor;

import com.ncist.common.annotation.Public;
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器。
 *
 * <p>拦截所有 {@code /api/**} 请求，完成以下校验：</p>
 * <ol>
 *   <li>放行 {@code OPTIONS} 预检请求；</li>
 *   <li>放行标注 {@link Public} 的公开接口；</li>
 *   <li>解析 {@code Authorization: Bearer <token>} 中的 JWT；</li>
 *   <li>与缓存中的服务端 token 做二次校验，支持登出、改密、重置密码、禁用后立即失效；</li>
 *   <li>将登录用户信息写入 {@link UserContext}，请求结束时清理。</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    /** 服务端登录令牌缓存前缀。 */
    private static final String TOKEN_PREFIX = "token:";

    /** Redis 模板，用于读取服务端登录令牌。 */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 请求处理前执行登录校验。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  请求处理器
     * @return {@code true} 表示放行，{@code false} 表示拦截
     * @throws BusinessException 未登录、token 过期或 token 无效时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(Public.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(Public.class)) {
                return true;
            }
        }
        String token = extractToken(request);
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));
        // JWT + 缓存双校验：token 必须与服务端记录一致
        String serverToken = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
        if (serverToken == null) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
        }
        if (!serverToken.equals(token)) {
            throw new BusinessException(AuthErrorCode.TOKEN_INVALID);
        }
        UserContext.UserInfo userInfo = new UserContext.UserInfo(
                userId,
                String.valueOf(claims.get("username")),
                Integer.valueOf(String.valueOf(claims.get("role")))
        );
        UserContext.set(userInfo);
        return true;
    }

    /**
     * 请求完成后清理线程上下文，避免线程复用导致用户信息串用。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  请求处理器
     * @param ex       请求处理过程中抛出的异常，可能为 {@code null}
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 从请求头中提取 Bearer token。
     *
     * @param request 当前请求
     * @return JWT 字符串
     * @throws BusinessException 请求头缺失或格式不合法时抛出 {@link AuthErrorCode#UNAUTHORIZED}
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer")) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }
        return header.substring(7);
    }
}
