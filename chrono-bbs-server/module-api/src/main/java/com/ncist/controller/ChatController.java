package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.ConversationCreateDTO;
import com.ncist.service.ChatService;
import com.ncist.vo.ConversationVO;
import com.ncist.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "即时通讯", description = "WebSocket 聊天相关的会话列表、消息历史等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        return Result.success(chatService.listConversations(UserContext.getUserId()));
    }

    @Operation(summary = "历史消息（从新到旧）")
    @Parameter(name = "conversationId", description = "会话 ID", example = "1")
    @GetMapping("/history/{conversationId}")
    public Result<PageResult<MessageVO>> history(@PathVariable Long conversationId,
                                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(required = false, defaultValue = "20") Integer size) {
        return Result.success(chatService.history(UserContext.getUserId(), conversationId, page, size));
    }

    @Operation(summary = "创建或获取会话")
    @PostMapping("/conversation")
    public Result<ConversationVO> create(@Valid @RequestBody ConversationCreateDTO dto) {
        return Result.success(chatService.createConversation(
                UserContext.getUserId(), dto.getMerchantId(), dto.getProductId()));
    }

    @Operation(summary = "标记会话已读")
    @PutMapping("/read/{conversationId}")
    public Result<Void> markRead(@PathVariable Long conversationId) {
        chatService.markRead(UserContext.getUserId(), conversationId);
        return Result.success();
    }

    @Operation(summary = "未读消息总数")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(chatService.unreadCount(UserContext.getUserId()));
    }
}
