package com.ncist.common.utils;

import com.ncist.common.enums.AuthErrorCode;
import com.ncist.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 登录令牌工具类。
 *
 * <p>负责签发与解析 JWT，令牌默认有效期为 2 小时。
 * 解析失败时统一转换为业务异常，由上层处理为 401 响应。</p>
 */
public class JwtUtil {
    /** 令牌有效期：2 小时（毫秒）。 */
    private static final long EXPIRED_TIME = 2 * 60 * 60 * 1000L;
    /** 令牌签名密钥。 */
    private static final SecretKey key = Keys.hmacShaKeyFor(
            "MySuperSecretKeyThatIsAtLeast256BitsLongForHS256".getBytes(StandardCharsets.UTF_8));

    /**
     * 生成登录令牌。
     *
     * @param claims 需要写入令牌的声明，如用户 ID、用户名、角色
     * @return 签名后的 JWT 字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(now.getTime() + EXPIRED_TIME))
                .subject("chrono-bbs")
                .issuedAt(now)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验登录令牌。
     *
     * @param token 待解析的 JWT 字符串
     * @return 令牌中的声明
     * @throws BusinessException 令牌过期时抛出 {@link AuthErrorCode#TOKEN_EXPIRED}，
     *                           格式错误或签名不合法时抛出 {@link AuthErrorCode#TOKEN_INVALID}
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.TOKEN_INVALID);
        }
    }
}