import service from '@/api'
import type { PageResult } from '@/types/common'

export interface Conversation {
  id: number
  targetId: number
  targetName: string
  targetAvatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
}

export interface ChatMessage {
  id: number
  conversationId: number
  senderId: number
  senderName: string
  content: string
  type: 'text' | 'image'
  createdAt: string
}

export interface SendMessageRequest {
  targetId: number
  content: string
  type: 'text' | 'image'
}

export const getConversations = () => {
  return service.get<any, { code: number; message: string; data: Conversation[]; timestamp: number }>('/chat/conversations')
}

export const getMessages = (conversationId: number, params?: { page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<ChatMessage>; timestamp: number }>(
    `/chat/history/${conversationId}`,
    { params }
  )
}

export const sendMessage = (data: SendMessageRequest) => {
  return service.post<any, { code: number; message: string; data: ChatMessage; timestamp: number }>('/chat/message', data)
}

export const createConversation = (data: { merchantId: number; productId?: number }) => {
  return service.post<any, { code: number; message: string; data: Conversation; timestamp: number }>('/chat/conversation', data)
}

export const markConversationRead = (conversationId: number) => {
  return service.put(`/chat/read/${conversationId}`)
}

export const getUnreadCount = () => {
  return service.get<any, { code: number; message: string; data: { count: number }; timestamp: number }>('/chat/unread-count')
}