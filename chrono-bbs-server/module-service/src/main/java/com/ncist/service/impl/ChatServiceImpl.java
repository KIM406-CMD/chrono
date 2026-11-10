package com.ncist.service.impl;

import com.ncist.common.enums.ChatErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.SendMessageDTO;
import com.ncist.entity.Conversation;
import com.ncist.entity.ConversationDetail;
import com.ncist.entity.Merchant;
import com.ncist.entity.Message;
import com.ncist.entity.MessageDetail;
import com.ncist.entity.User;
import com.ncist.mapper.ConversationMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.MessageMapper;
import com.ncist.mapper.UserMapper;
import com.ncist.service.ChatService;
import com.ncist.vo.ConversationVO;
import com.ncist.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 即时通讯服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** 商家审核通过 */
    private static final int MERCHANT_APPROVED = 1;
    /** 消息类型：1-文本，2-图片 */
    private static final int MESSAGE_TYPE_TEXT = 1;
    private static final int MESSAGE_TYPE_IMAGE = 2;
    /** 发送者角色：0-用户，1-商家 */
    private static final int ROLE_USER = 0;
    private static final int ROLE_MERCHANT = 1;
    /** 会话最后消息摘要最大长度（对应 conversation.last_message VARCHAR(500)） */
    private static final int LAST_MESSAGE_MAX_LENGTH = 500;
    /** 消息内容最大长度（对应 message.content VARCHAR(2000)） */
    private static final int MESSAGE_MAX_LENGTH = 2000;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        return conversationMapper.selectDetailByUser(userId).stream()
                .map(detail -> toConversationVO(detail, userId))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<MessageVO> history(Long userId, Long conversationId, Integer page, Integer size) {
        Conversation conversation = requireParticipant(conversationId, userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        // 进入会话即视为已读
        messageMapper.markRead(conversationId, userId);
        conversationMapper.resetUnread(conversation.getId(), userId);
        long total = messageMapper.countByConversation(conversationId);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<MessageDetail> list = messageMapper.selectPage(conversationId, (p - 1) * s, s);
        return PageResult.of(total, p, s, list.stream().map(this::toMessageVO).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO createConversation(Long userId, Long merchantId, Long productId) {
        if (userId.equals(merchantId)) {
            throw new BusinessException(ChatErrorCode.CANNOT_CHAT_SELF);
        }
        Merchant merchant = merchantMapper.selectByUserId(merchantId);
        if (merchant == null) {
            throw new BusinessException(ChatErrorCode.MERCHANT_NOT_FOUND);
        }
        if (!Integer.valueOf(MERCHANT_APPROVED).equals(merchant.getStatus())) {
            throw new BusinessException(ChatErrorCode.MERCHANT_INVALID);
        }
        Conversation existing = conversationMapper.selectByUserAndMerchant(userId, merchantId);
        if (existing != null) {
            return toConversationVO(conversationMapper.selectDetailById(existing.getId()), userId);
        }
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .merchantId(merchantId)
                .productId(productId)
                .build();
        try {
            conversationMapper.insert(conversation);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底：并发创建时返回已存在会话
            Conversation duplicated = conversationMapper.selectByUserAndMerchant(userId, merchantId);
            return toConversationVO(conversationMapper.selectDetailById(duplicated.getId()), userId);
        }
        return toConversationVO(conversationMapper.selectDetailById(conversation.getId()), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long conversationId) {
        Conversation conversation = requireParticipant(conversationId, userId);
        messageMapper.markRead(conversationId, userId);
        conversationMapper.resetUnread(conversation.getId(), userId);
    }

    @Override
    public Map<String, Long> unreadCount(Long userId) {
        return Map.of("count", conversationMapper.sumUnread(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long senderId, SendMessageDTO dto) {
        if (dto == null || dto.getConversationId() == null) {
            throw new BusinessException(ChatErrorCode.CONVERSATION_NOT_FOUND);
        }
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException(ChatErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        if (content.length() > MESSAGE_MAX_LENGTH) {
            throw new BusinessException(ChatErrorCode.MESSAGE_TOO_LONG);
        }
        int type = parseType(dto.getType());

        Conversation conversation = requireParticipant(dto.getConversationId(), senderId);
        int senderRole = senderId.equals(conversation.getUserId()) ? ROLE_USER : ROLE_MERCHANT;
        LocalDateTime now = LocalDateTime.now();

        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .senderRole(senderRole)
                .content(content)
                .type(type)
                .isRead(0)
                .build();
        messageMapper.insert(message);

        // 更新最后消息摘要，并给对方未读数 +1
        String summary = content.length() > LAST_MESSAGE_MAX_LENGTH
                ? content.substring(0, LAST_MESSAGE_MAX_LENGTH) : content;
        int userUnreadInc = senderRole == ROLE_MERCHANT ? 1 : 0;
        int merchantUnreadInc = senderRole == ROLE_USER ? 1 : 0;
        conversationMapper.updateLastMessage(conversation.getId(), summary, now, userUnreadInc, merchantUnreadInc);

        return MessageVO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(senderId)
                .senderName(senderName(senderId))
                .senderRole(senderRole)
                .content(content)
                .type(typeToName(type))
                .isRead(0)
                .createdAt(now)
                .build();
    }

    @Override
    public List<MessageVO> listUnreadMessages(Long userId) {
        return messageMapper.selectUnreadForUser(userId).stream()
                .map(this::toMessageVO)
                .toList();
    }

    @Override
    public Long conversationPeer(Long conversationId, Long userId) {
        Conversation conversation = requireParticipant(conversationId, userId);
        return userId.equals(conversation.getUserId())
                ? conversation.getMerchantId() : conversation.getUserId();
    }

    // ================ 私有校验与组装 ================

    private Conversation requireParticipant(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ChatErrorCode.CONVERSATION_NOT_FOUND);
        }
        if (!userId.equals(conversation.getUserId()) && !userId.equals(conversation.getMerchantId())) {
            throw new BusinessException(ChatErrorCode.NO_PERMISSION_ACCESS);
        }
        return conversation;
    }

    private int parseType(String type) {
        if (type == null || type.isBlank() || "TEXT".equalsIgnoreCase(type)) {
            return MESSAGE_TYPE_TEXT;
        }
        if ("IMAGE".equalsIgnoreCase(type)) {
            return MESSAGE_TYPE_IMAGE;
        }
        throw new BusinessException(ChatErrorCode.MESSAGE_TYPE_INVALID);
    }

    private String typeToName(Integer type) {
        return Integer.valueOf(MESSAGE_TYPE_IMAGE).equals(type) ? "IMAGE" : "TEXT";
    }

    private String senderName(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname() : user.getUsername();
    }

    private ConversationVO toConversationVO(ConversationDetail detail, Long currentUserId) {
        if (detail == null) {
            return null;
        }
        int unread = currentUserId.equals(detail.getUserId())
                ? (detail.getUserUnread() == null ? 0 : detail.getUserUnread())
                : (detail.getMerchantUnread() == null ? 0 : detail.getMerchantUnread());
        return ConversationVO.builder()
                .id(detail.getId())
                .userId(detail.getUserId())
                .merchantId(detail.getMerchantId())
                .merchantName(detail.getMerchantName())
                .merchantAvatar(detail.getMerchantAvatar())
                .productId(detail.getProductId())
                .productName(detail.getProductName())
                .lastMessage(detail.getLastMessage())
                .lastMessageTime(detail.getLastMessageTime())
                .unreadCount(unread)
                .build();
    }

    private MessageVO toMessageVO(MessageDetail message) {
        return MessageVO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .type(typeToName(message.getType()))
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
