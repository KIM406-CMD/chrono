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

public class JwtUtil {
    //token过期时间2h
    private static final long EXPIRED_TIME=2*60*60*1000L;
    //token密钥
    private static final SecretKey key= Keys.hmacShaKeyFor( "MySuperSecretKeyThatIsAtLeast256BitsLongForHS256".getBytes(StandardCharsets.UTF_8));
    //生成token
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
    //解析token
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