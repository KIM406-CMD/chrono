package com.ncist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.service.ChatService;
import com.ncist.service.RedisOnlineService;
import com.ncist.websocket.ChatWebSocketEndpoint;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 原生 WebSocket 配置：注册 @ServerEndpoint 并注入端点所需 Spring Bean
 * <p>test 环境（Mock ServletContext）没有 ServerContainer，跳过导出以免启动失败。</p>
 */
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class WebSocketConfig {

    private final ChatService chatService;
    private final RedisOnlineService redisOnlineService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void injectEndpointDependencies() {
        ChatWebSocketEndpoint.setDependencies(chatService, redisOnlineService, objectMapper);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
