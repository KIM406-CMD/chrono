package com.ncist.unit.service;

import com.ncist.common.enums.ChatErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.SendMessageDTO;
import com.ncist.entity.Conversation;
import com.ncist.entity.Message;
import com.ncist.entity.User;
import com.ncist.mapper.ConversationMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.MessageMapper;
import com.ncist.mapper.UserMapper;
import com.ncist.service.impl.ChatServiceImpl;
import com.ncist.vo.MessageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("即时通讯服务单元测试")
class ChatServiceImplTest {

    private static final Long USER_ID = 4L;
    private static final Long MERCHANT_USER_ID = 2L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private ConversationMapper conversationMapper;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private Conversation conversation() {
        return Conversation.builder()
                .id(CONVERSATION_ID)
                .userId(USER_ID)
                .merchantId(MERCHANT_USER_ID)
                .build();
    }

    private SendMessageDTO messageDTO(String content) {
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONVERSATION_ID);
        dto.setContent(content);
        dto.setType("TEXT");
        return dto;
    }

    @Test
    @DisplayName("发送消息-用户发送成功并增加商家未读数")
    void sendMessageAsUser() {
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conversation());
        when(userMapper.selectById(USER_ID))
                .thenReturn(User.builder().id(USER_ID).username("user1").nickname("张小明").build());

        MessageVO vo = chatService.sendMessage(USER_ID, messageDTO("  你好  "));

        assertEquals("你好", vo.getContent());
        assertEquals(0, vo.getSenderRole());
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals("你好", captor.getValue().getContent());
        assertEquals(0, captor.getValue().getSenderRole());
        verify(conversationMapper).updateLastMessage(
                eq(CONVERSATION_ID), eq("你好"), any(LocalDateTime.class), eq(0), eq(1));
    }

    @Test
    @DisplayName("发送消息-内容为空返回400")
    void sendMessageEmpty() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(USER_ID, messageDTO("   ")));

        assertEquals(ChatErrorCode.MESSAGE_CONTENT_EMPTY.getCode(), e.getCode());
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("发送消息-非会话参与者返回403")
    void sendMessageNonParticipant() {
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conversation());

        BusinessException e = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(99L, messageDTO("你好")));

        assertEquals(ChatErrorCode.NO_PERMISSION_ACCESS.getCode(), e.getCode());
    }

    @Test
    @DisplayName("创建会话-不能与自己发起会话")
    void createConversationWithSelf() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> chatService.createConversation(USER_ID, USER_ID, 10001L));

        assertEquals(ChatErrorCode.CANNOT_CHAT_SELF.getCode(), e.getCode());
        verify(conversationMapper, never()).selectByUserAndMerchant(any(), any());
    }

    @Test
    @DisplayName("历史消息-空会话仍标记已读并返回空分页")
    void historyEmptyConversation() {
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conversation());
        when(messageMapper.countByConversation(CONVERSATION_ID)).thenReturn(0L);

        PageResult<MessageVO> result = chatService.history(USER_ID, CONVERSATION_ID, 1, 20);

        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getList().size());
        verify(messageMapper).markRead(CONVERSATION_ID, USER_ID);
        verify(conversationMapper).resetUnread(CONVERSATION_ID, USER_ID);
    }

    @Test
    @DisplayName("会话对方-用户查询时返回商家用户 ID")
    void conversationPeer() {
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conversation());

        assertEquals(MERCHANT_USER_ID, chatService.conversationPeer(CONVERSATION_ID, USER_ID));
        assertEquals(USER_ID, chatService.conversationPeer(CONVERSATION_ID, MERCHANT_USER_ID));
    }
}
