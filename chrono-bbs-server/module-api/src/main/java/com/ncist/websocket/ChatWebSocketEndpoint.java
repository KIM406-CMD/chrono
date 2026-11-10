package com.ncist.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.utils.JwtUtil;
import com.ncist.dto.SendMessageDTO;
import com.ncist.service.ChatService;
import com.ncist.service.RedisOnlineService;
import com.ncist.vo.MessageVO;
import io.jsonwebtoken.Claims;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原生 WebSocket 聊天端点：ws://host/chat/{userId}?token=jwt
 */
@Slf4j
@Component
@ServerEndpoint("/chat/{userId}")
public class ChatWebSocketEndpoint {

    /** 本地在线连接（userId -> Session），用于实时推送 */
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    private static ChatService chatService;
    private static RedisOnlineService redisOnlineService;
    private static ObjectMapper objectMapper;

    private Session session;
    private Long userId;

    /** 由 WebSocketConfig 启动时注入 Spring Bean */
    public static void setDependencies(ChatService service, RedisOnlineService onlineService, ObjectMapper mapper) {
        chatService = service;
        redisOnlineService = onlineService;
        objectMapper = mapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdText) {
        this.session = session;
        try {
            Long pathUserId = Long.valueOf(userIdText);
            String token = extractToken(session.getQueryString());
            Claims claims = JwtUtil.parseToken(token);
            Long tokenUserId = Long.valueOf(String.valueOf(claims.get("userId")));
            // 防冒连：token 中的 userId 必须与路径一致，且与服务端 Redis 记录一致
            if (!pathUserId.equals(tokenUserId) || !redisOnlineService.isTokenValid(tokenUserId, token)) {
                closeWithPolicyViolation();
                return;
            }
            this.userId = pathUserId;
            Session previous = SESSIONS.put(pathUserId, session);
            if (previous != null && previous.isOpen()) {
                previous.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "账号在其他位置连接"));
            }
            redisOnlineService.setOnline(pathUserId);
            log.info("WebSocket 连接建立: userId={}", pathUserId);
            pushOfflineMessages(pathUserId);
        } catch (Exception e) {
            log.warn("WebSocket 连接鉴权失败: userId={}, reason={}", userIdText, e.getMessage());
            closeWithPolicyViolation();
        }
    }

    @OnMessage
    public void onMessage(String text) {
        if (userId == null) {
            return;
        }
        try {
            SendMessageDTO dto = objectMapper.readValue(text, SendMessageDTO.class);
            MessageVO message = chatService.sendMessage(userId, dto);
            String payload = objectMapper.writeValueAsString(toFrame(message, dto.getProductId()));
            // 回显给发送方
            sendToSession(session, payload);
            // 实时推送给接收方（离线则仅入库，上线后补推）
            Long receiverId = chatService.conversationPeer(dto.getConversationId(), userId);
            Session receiver = SESSIONS.get(receiverId);
            if (receiver != null && receiver.isOpen()) {
                sendToSession(receiver, payload);
            }
        } catch (BusinessException e) {
            sendError(e.getMessage());
        } catch (Exception e) {
            log.error("WebSocket 消息处理失败: userId={}", userId, e);
            sendError("消息发送失败");
        }
    }

    @OnClose
    public void onClose() {
        if (userId == null) {
            return;
        }
        boolean removed = SESSIONS.remove(userId, session);
        if (removed) {
            redisOnlineService.setOffline(userId);
            log.info("WebSocket 连接关闭: userId={}", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 异常: userId={}", userId, error);
        try {
            session.close();
        } catch (Exception ignored) {
            // 连接已失效，忽略关闭异常
        }
    }

    // ================ 私有工具 ================

    private void pushOfflineMessages(Long userId) throws Exception {
        for (MessageVO message : chatService.listUnreadMessages(userId)) {
            sendToSession(session, objectMapper.writeValueAsString(toFrame(message, null)));
        }
    }

    private Map<String, Object> toFrame(MessageVO message, Long productId) {
        Map<String, Object> frame = new LinkedHashMap<>();
        frame.put("type", message.getType());
        frame.put("conversationId", message.getConversationId());
        frame.put("messageId", message.getId());
        frame.put("senderId", message.getSenderId());
        frame.put("senderRole", message.getSenderRole());
        frame.put("content", message.getContent());
        frame.put("productId", productId);
        frame.put("isRead", message.getIsRead());
        frame.put("timestamp", System.currentTimeMillis());
        return frame;
    }

    private void sendError(String message) {
        try {
            sendToSession(session, objectMapper.writeValueAsString(Map.of("type", "ERROR", "message", message)));
        } catch (Exception e) {
            log.warn("WebSocket 错误消息发送失败: userId={}", userId, e);
        }
    }

    private void sendToSession(Session target, String payload) throws Exception {
        if (target == null || !target.isOpen()) {
            return;
        }
        synchronized (target) {
            target.getBasicRemote().sendText(payload);
        }
    }

    private void closeWithPolicyViolation() {
        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "身份校验失败"));
            }
        } catch (Exception ignored) {
            // 连接已失效，忽略关闭异常
        }
    }

    /** 从握手 queryString 中提取 token 参数 */
    private static String extractToken(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return null;
        }
        for (String pair : queryString.split("&")) {
            int index = pair.indexOf('=');
            if (index > 0 && "token".equals(pair.substring(0, index))) {
                return URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
