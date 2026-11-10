package com.ncist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 即时通讯在线状态与连接鉴权（Redis）
 */
@Service
@RequiredArgsConstructor
public class RedisOnlineService {

    private static final String ONLINE_PREFIX = "online:";
    private static final String TOKEN_PREFIX = "token:";

    private final StringRedisTemplate stringRedisTemplate;

    public void setOnline(Long userId) {
        stringRedisTemplate.opsForValue().set(ONLINE_PREFIX + userId, "1");
    }

    public void setOffline(Long userId) {
        stringRedisTemplate.delete(ONLINE_PREFIX + userId);
    }

    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(ONLINE_PREFIX + userId));
    }

    /** 与 JwtInterceptor 一致：WebSocket 握手 token 必须与服务端记录一致 */
    public boolean isTokenValid(Long userId, String token) {
        if (token == null) {
            return false;
        }
        String serverToken = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
        return serverToken != null && serverToken.equals(token);
    }
}
