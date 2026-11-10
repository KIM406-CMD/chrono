package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.SendMessageDTO;
import com.ncist.vo.ConversationVO;
import com.ncist.vo.MessageVO;

import java.util.List;
import java.util.Map;

public interface ChatService {

    /** 当前用户参与的全部会话，按最后消息时间倒序 */
    List<ConversationVO> listConversations(Long userId);

    /** 历史消息（从新到旧），进入会话即标记对方消息已读 */
    PageResult<MessageVO> history(Long userId, Long conversationId, Integer page, Integer size);

    /** 创建或获取与指定商家的会话 */
    ConversationVO createConversation(Long userId, Long merchantId, Long productId);

    /** 标记会话中对方发来的消息为已读 */
    void markRead(Long userId, Long conversationId);

    /** 当前用户未读消息总数 */
    Map<String, Long> unreadCount(Long userId);

    /** 发送消息（WebSocket 与内部调用共用） */
    MessageVO sendMessage(Long senderId, SendMessageDTO dto);

    /** 当前用户离线期间的未读消息（WebSocket 上线补推） */
    List<MessageVO> listUnreadMessages(Long userId);

    /** 会话中另一方（WebSocket 消息路由用），同时校验当前用户是否属于该会话 */
    Long conversationPeer(Long conversationId, Long userId);
}
