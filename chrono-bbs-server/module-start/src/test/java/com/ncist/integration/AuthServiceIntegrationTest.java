package com.ncist.integration;

import com.ncist.service.AuthService;
import com.ncist.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthService 集成测试")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("getCaptcha 应该真正写入 Redis")
    void shouldWriteCaptchaToRedis() {
        CaptchaVO captchaVO = authService.getCaptcha();

        String key = "captcha:" + captchaVO.getUuid();
        String codeInRedis = stringRedisTemplate.opsForValue().get(key);

        assertNotNull(codeInRedis, "Redis 中应该存在 key: " + key);
        assertFalse(codeInRedis.isEmpty(), "验证码不应为空");
        assertEquals(4, codeInRedis.length(), "验证码应为 4 位");

        log.info("=== Redis 写入成功 ===");
        log.info("Key : {}", key);
        log.info("Code: {}", codeInRedis);
        log.info("Image (贴到浏览器地址栏即可查看): {}", captchaVO.getImage());

        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 120, "TTL 应在 1~120 秒之间，实际: " + ttl);
        log.info("TTL: {} 秒", ttl);
    }
}