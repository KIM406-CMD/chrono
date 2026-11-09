package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 即时通讯控制器。
 *
 * <p>用于承载 WebSocket 聊天相关的会话列表与消息历史等 HTTP 接口；
 * 当前聊天能力由 WebSocket 通道实现，本控制器暂无对外方法。
 * 接口前缀 {@code /api/chat}。</p>
 */
@Tag(name = "即时通讯", description = "WebSocket 聊天相关的会话列表、消息历史等接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
}