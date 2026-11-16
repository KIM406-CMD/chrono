import service from '@/api'
import type { PageResult } from '@/types/common'

/** 站内通知 */
export interface NotificationItem {
  id: number
  /** 1-订单通知，2-系统通知，3-售后通知 */
  type: number
  title: string
  content: string
  /** 1-订单，2-退款 */
  relatedType?: number
  relatedId?: number
  /** 0-未读，1-已读 */
  isRead: number
  createdAt: string
}

export const getNotifications = (params: { page?: number; size?: number; unreadOnly?: boolean }) => {
  return service.get<any, { code: number; message: string; data: PageResult<NotificationItem>; timestamp: number }>(
    '/notification/list',
    { params },
  )
}

export const getNotificationUnreadCount = () => {
  return service.get<any, { code: number; message: string; data: { count: number }; timestamp: number }>(
    '/notification/unread-count',
  )
}

export const markNotificationRead = (id: number) => {
  return service.put(`/notification/${id}/read`)
}

export const markAllNotificationsRead = () => {
  return service.put<any, { code: number; message: string; data: number; timestamp: number }>(
    '/notification/read-all',
  )
}
