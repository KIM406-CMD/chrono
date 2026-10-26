package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "即时通讯", description = "WebSocket 聊天相关的会话列表、消息历史等接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
}