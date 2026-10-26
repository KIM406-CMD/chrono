package com.ncist.common.utils;

import com.ncist.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private static final String USER_ID = "1";
    private static final String USERNAME = "testUser";

    @Test
    @DisplayName("生成token并解析，可正确取出claims")
    void shouldGenerateAndParseToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);
        claims.put("username", USERNAME);

        String token = JwtUtil.generateToken(claims);
        assertNotNull(token);

        Claims parsedClaims = JwtUtil.parseToken(token);
        assertEquals(USER_ID, parsedClaims.get("userId"));
        assertEquals(USERNAME, parsedClaims.get("username"));
        assertEquals("chrono-bbs", parsedClaims.getSubject());
        log.info("parsedClaims: {}", parsedClaims);
    }

    @Test
    @DisplayName("解析过期token抛出BusinessException: token已过期")
    void shouldThrowExceptionWhenTokenExpired() {
        SecretKey tempKey = Keys.hmacShaKeyFor(
                "MySuperSecretKeyThatIsAtLeast256BitsLongForHS256"
                        .getBytes(StandardCharsets.UTF_8)
        );
        String expiredToken = Jwts.builder()
                .subject("chrono-bbs")
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(tempKey)
                .compact();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> JwtUtil.parseToken(expiredToken));
        assertEquals(401, ex.getCode());
        assertEquals("token已过期", ex.getMessage());
        log.error(ex.getMessage(), ex);
    }

    @Test
    @DisplayName("解析格式错误的token抛出BusinessException: token无效")
    void shouldThrowExceptionWhenTokenMalformed() {
        String malformedToken = "this.is.not.a.valid.token";

        BusinessException ex = assertThrows(BusinessException.class,
                () -> JwtUtil.parseToken(malformedToken));
        assertEquals(401, ex.getCode());
        assertEquals("token无效", ex.getMessage());
        log.error(ex.getMessage(), ex);
    }

    @Test
    @DisplayName("解析签名错误的token抛出BusinessException: token无效")
    void shouldThrowExceptionWhenSignatureInvalid() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "WrongSecretKeyThatIsAtLeast256BitsLong!!"
                        .getBytes(StandardCharsets.UTF_8)
        );
        String tamperedToken = Jwts.builder()
                .subject("chrono-bbs")
                .signWith(wrongKey)
                .compact();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> JwtUtil.parseToken(tamperedToken));
        assertEquals(401, ex.getCode());
        assertEquals("token无效", ex.getMessage());
        log.error(ex.getMessage(), ex);
    }

    @Test
    @DisplayName("解析空字符串抛出BusinessException: token无效")
    void shouldThrowExceptionWhenTokenEmpty() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> JwtUtil.parseToken(""));
        assertEquals(401, ex.getCode());
        assertEquals("token无效", ex.getMessage());
        log.error(ex.getMessage(), ex);
    }
}