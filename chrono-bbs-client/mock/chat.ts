import { MockMethod } from 'vite-plugin-mock'

const conversations = [
  {
    id: 1,
    targetId: 101,
    targetName: '测试店铺客服',
    targetAvatar: '',
    lastMessage: '您好，有什么可以帮助您的？',
    lastTime: '2025-01-02 10:00:00',
    unreadCount: 2,
  },
]

const messages: any[] = [
  { id: 1, conversationId: 1, senderId: 101, senderName: '测试店铺客服', content: '您好，有什么可以帮助您的？', type: 'text', createdAt: '2025-01-02 09:55:00' },
  { id: 2, conversationId: 1, senderId: 100, senderName: '我', content: '请问这个商品有货吗？', type: 'text', createdAt: '2025-01-02 09:58:00' },
  { id: 3, conversationId: 1, senderId: 101, senderName: '测试店铺客服', content: '有的，亲可以在商品详情页直接下单哦~', type: 'text', createdAt: '2025-01-02 10:00:00' },
]

export default [
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
    url: '/api/chat/:conversationId/messages',
    method: 'get',
    timeout: 300,
    response: ({ query }: any) => {
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
    url: '/api/chat/message',
    method: 'post',
    timeout: 300,
    response: ({ body }: any) => {
      const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
      const msg = {
        id: messages.length + 1,
        conversationId: 1,
        senderId: 100,
        senderName: '我',
        content: body.content,
        type: body.type || 'text',
        createdAt: now,
      }
      messages.push(msg)
      return { code: 200, message: '发送成功', data: msg, timestamp: Date.now() }
    },
  },
  {
    url: '/api/chat/conversation',
    method: 'post',
    timeout: 300,
    response: () => {
      const conv = {
        id: conversations.length + 1,
        targetId: 102,
        targetName: '新对话客服',
        targetAvatar: '',
        lastMessage: '',
        lastTime: '',
        unreadCount: 0,
      }
      conversations.push(conv)
      return { code: 200, message: 'success', data: conv, timestamp: Date.now() }
    },
  },
] as MockMethod[]