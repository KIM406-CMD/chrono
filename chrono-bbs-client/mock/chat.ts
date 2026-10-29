import { MockMethod } from 'vite-plugin-mock'

const conversations = [
  {
    id: 1,
    userId: 100,
    merchantId: 101,
    merchantName: '测试店铺客服',
    merchantAvatar: '',
    productId: 1,
    productName: '测试商品',
    lastMessage: '您好，有什么可以帮助您的？',
    lastMessageTime: '2025-01-02T10:00:00',
    unreadCount: 2,
  },
]

const messages: any[] = [
  { id: 1, conversationId: 1, senderId: 101, senderName: '测试店铺客服', content: '您好，有什么可以帮助您的？', type: 'text', createdAt: '2025-01-02 09:55:00' },
  { id: 2, conversationId: 1, senderId: 100, senderName: '我', content: '请问这个商品有货吗？', type: 'text', createdAt: '2025-01-02 09:58:00' },
  { id: 3, conversationId: 1, senderId: 101, senderName: '测试店铺客服', content: '有的，亲可以在商品详情页直接下单哦~', type: 'text', createdAt: '2025-01-02 10:00:00' },
]

const mockMethods: MockMethod[] = [
  {
    url: '/api/chat/conversations',
    method: 'get',
    timeout: 300,
    response: () => ({
      code: 200,
      message: 'success',
      data: conversations,
      timestamp: Date.now(),
    }),
  },
  {
    url: '/api/chat/history/:conversationId',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const convId = parseInt(query.conversationId)
      const list = messages.filter((m) => m.conversationId === convId)
      return {
        code: 200,
        message: 'success',
        data: { total: list.length, page: 1, size: 50, list },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/chat/read/:conversationId',
    method: 'put',
    timeout: 300,
    response: () => {
      return { code: 200, message: 'success', data: null, timestamp: Date.now() }
    },
  },
  {
    url: '/api/chat/unread-count',
    method: 'get',
    timeout: 300,
    response: () => {
      return { code: 200, message: 'success', data: { count: 2 }, timestamp: Date.now() }
    },
  },
  {
    url: '/api/chat/conversation',
    method: 'post',
    timeout: 300,
    response: () => {
      const conv = {
        id: conversations.length + 1,
        merchantId: 102,
        merchantName: '新对话客服',
        merchantAvatar: '',
        lastMessage: '',
        lastMessageTime: '',
        unreadCount: 0,
      }
      conversations.push(conv)
      return { code: 200, message: 'success', data: conv, timestamp: Date.now() }
    },
  },
]
export default mockMethods