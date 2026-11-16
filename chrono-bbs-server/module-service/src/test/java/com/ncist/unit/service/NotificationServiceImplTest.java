package com.ncist.unit.service;

import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.entity.Notification;
import com.ncist.mapper.NotificationMapper;
import com.ncist.service.impl.NotificationServiceImpl;
import com.ncist.vo.NotificationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NotificationServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("站内通知服务单元测试")
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("通知列表-返回分页与未读状态")
    void listSuccess() {
        when(notificationMapper.countByUser(4L, false)).thenReturn(1L);
        when(notificationMapper.selectPage(4L, false, 0, 10)).thenReturn(List.of(
                Notification.builder().id(1L).userId(4L).type(1).title("订单提交成功")
                        .content("内容").relatedType(1).relatedId(50005L).isRead(0)
                        .createdAt(LocalDateTime.now()).build()));

        PageResult<NotificationVO> result = notificationService.list(4L, null, 1, 10);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("订单提交成功", result.getList().get(0).getTitle());
        assertEquals(0, result.getList().get(0).getIsRead());
    }

    @Test
    @DisplayName("通知列表-空结果不查询明细")
    void listEmpty() {
        when(notificationMapper.countByUser(4L, true)).thenReturn(0L);

        PageResult<NotificationVO> result = notificationService.list(4L, true, 1, 10);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("标记已读-通知不存在或不属于当前用户时抛出业务异常")
    void markReadNotFound() {
        when(notificationMapper.markRead(99L, 4L)).thenReturn(0);

        assertThrows(BusinessException.class, () -> notificationService.markRead(4L, 99L));
    }

    @Test
    @DisplayName("全部已读-返回本次标记数量")
    void markAllRead() {
        when(notificationMapper.markAllRead(4L)).thenReturn(3);

        assertEquals(3, notificationService.markAllRead(4L));
    }

    @Test
    @DisplayName("下单通知-写入订单类型与关联订单")
    void notifyOrderCreated() {
        notificationService.notifyOrderCreated(4L, 50005L, "20261116", 299900L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(4L, saved.getUserId());
        assertEquals(1, saved.getType());
        assertEquals(1, saved.getRelatedType());
        assertEquals(50005L, saved.getRelatedId());
        assertEquals(0, saved.getIsRead());
        assertTrue(saved.getContent().contains("¥2999.00"));
    }
}
