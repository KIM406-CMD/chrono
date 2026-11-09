import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

// 当前 Mock 登录用户
const MOCK_USER_ID = 100
const MOCK_USER_NAME = '我'

const conversations: any[] = [
  {
    id: 1,
    userId: MOCK_USER_ID,
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
  { id: 2, conversationId: 1, senderId: MOCK_USER_ID, senderName: MOCK_USER_NAME, content: '请问这个商品有货吗？', type: 'text', createdAt: '2025-01-02 09:58:00' },
  { id: 3, conversationId: 1, senderId: 101, senderName: '测试店铺客服', content: '有的，亲可以在商品详情页直接下单哦~', type: 'text', createdAt: '2025-01-02 10:00:00' },
]

const nowText = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

const mockMethods: MockMethod[] = [
  // 14.1 会话列表
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
  // 14.2 历史消息
  {
    url: '/api/chat/history/:conversationId',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const convId = parseInt(query.conversationId)
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 20
      const list = messages.filter((message) => message.conversationId === convId)
      return {
        code: 200,
        message: 'success',
        data: {
          total: list.length,
          page,
          size,
          list: list.slice((page - 1) * size, page * size),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 14.3 创建/获取会话
  {
    url: '/api/chat/conversation',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const merchantId = Number(body.merchantId)
      const productId = body.productId ? Number(body.productId) : undefined
      const existing = conversations.find(
        (conv) => conv.userId === MOCK_USER_ID && conv.merchantId === merchantId,
      )
      if (existing) {
        return { code: 200, message: 'success', data: existing, timestamp: Date.now() }
      }

      const product = productId ? allProducts.find((item) => item.id === productId) : undefined
      const conv = {
        id: conversations.length + 1,
        userId: MOCK_USER_ID,
        merchantId,
        merchantName: product?.merchantName || `店铺${merchantId}客服`,
        merchantAvatar: '',
        productId,
        productName: product?.name || '',
        lastMessage: '',
        lastMessageTime: nowText(),
        unreadCount: 0,
      }
      conversations.unshift(conv)
      return { code: 200, message: 'success', data: conv, timestamp: Date.now() }
    },
  },
  // 14.4 标记已读
  {
    url: '/api/chat/read/:conversationId',
    method: 'put',
    timeout: 300,
    response: ({ query }) => {
      const conv = conversations.find((item) => item.id === parseInt(query.conversationId))
      if (conv) conv.unreadCount = 0
      return { code: 200, message: 'success', data: null, timestamp: Date.now() }
    },
  },
  // 14.5 未读消息总数
  {
    url: '/api/chat/unread-count',
    method: 'get',
    timeout: 300,
    response: () => ({
      code: 200,
      message: 'success',
      data: {
        count: conversations.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0),
      },
      timestamp: Date.now(),
    }),
  },
  // 14.6 发送消息（Mock 扩展，后端正式实现走 WebSocket）
  {
    url: '/api/chat/message',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const { conversationId, content, type } = body
      const msg = {
        id: messages.length + 1,
        conversationId,
        senderId: MOCK_USER_ID,
        senderName: MOCK_USER_NAME,
        content,
        type: type || 'text',
        createdAt: nowText(),
      }
      messages.push(msg)
      const conv = conversations.find((item) => item.id === conversationId)
      if (conv) {
        conv.lastMessage = content
        conv.lastMessageTime = msg.createdAt
      }
      return { code: 200, message: 'success', data: msg, timestamp: Date.now() }
    },
  },
]
export default mockMethods
