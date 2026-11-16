import { MockMethod } from 'vite-plugin-mock'

const notifications = [
  {
    id: 1,
    type: 1,
    title: '订单提交成功',
    content: '订单 20261116194833686836 已提交，应付金额 ¥2999.00，请尽快完成支付。',
    relatedType: 1,
    relatedId: 50005,
    isRead: 0,
    createdAt: '2026-11-16T19:48:33',
  },
  {
    id: 2,
    type: 1,
    title: '订单支付成功',
    content: '订单 20261116194833686836 已支付 ¥2999.00，等待商家发货。',
    relatedType: 1,
    relatedId: 50005,
    isRead: 1,
    createdAt: '2026-11-16T19:48:35',
  },
]

const mockMethods: MockMethod[] = [
  {
    url: '/api/notification/list',
    method: 'get',
    timeout: 200,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const onlyUnread = query.unreadOnly === 'true'
      const list = onlyUnread ? notifications.filter((item) => item.isRead === 0) : notifications
      const start = (page - 1) * size
      return {
        code: 200,
        message: 'success',
        data: { total: list.length, page, size, list: list.slice(start, start + size) },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/notification/unread-count',
    method: 'get',
    timeout: 100,
    response: () => ({
      code: 200,
      message: 'success',
      data: { count: notifications.filter((item) => item.isRead === 0).length },
      timestamp: Date.now(),
    }),
  },
  {
    url: '/api/notification/:id/read',
    method: 'put',
    timeout: 100,
    response: ({ query }) => {
      const id = Number(query.id)
      const item = notifications.find((n) => n.id === id)
      if (item) item.isRead = 1
      return { code: 200, message: 'success', data: null, timestamp: Date.now() }
    },
  },
  {
    url: '/api/notification/read-all',
    method: 'put',
    timeout: 100,
    response: () => {
      let count = 0
      notifications.forEach((item) => {
        if (item.isRead === 0) {
          item.isRead = 1
          count++
        }
      })
      return { code: 200, message: 'success', data: count, timestamp: Date.now() }
    },
  },
]

export default mockMethods
