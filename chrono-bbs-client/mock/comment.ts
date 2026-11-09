import { MockMethod } from 'vite-plugin-mock'

// 本次 Mock 会话中用户新发表的评论（含图片）
const createdComments: any[] = []

const mockMethods: MockMethod[] = [
  // 9.1 商品评论列表
  {
    url: '/api/comment/list/:productId',
    method: 'get',
    timeout: 300,
    response: ({ query, url }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const pathMatch = /\/api\/comment\/list\/(\d+)/.exec(url || '')
      const productId = pathMatch ? parseInt(pathMatch[1]) : (parseInt(query.productId) || 0)
      
      const generated = Array.from({ length: 20 }, (_, i) => ({
        id: i + 1,
        productId,
        userId: (i % 10) + 1,
        username: `user${(i % 10) + 1}`,
        nickname: `用户${(i % 10) + 1}`,
        avatar: '',
        content: `这是第${i + 1}条评论，商品质量很好，物流也很快。`,
        star: 5 - (i % 3), // 3-5星
        images: [],
        createdAt: '2025-01-01 10:00:00',
      }))
      const mockComments = [
        ...createdComments.filter((item) => item.productId === productId),
        ...generated,
      ]

      const total = mockComments.length
      const start = (page - 1) * size
      const end = start + size
      const list = mockComments.slice(start, end)
      
      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page,
          size,
          list,
        },
        timestamp: Date.now(),
      }
    },
  },
  // 9.2 发表评论
  {
    url: '/api/comment',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const comment = {
        id: 1000 + createdComments.length,
        productId: Number(body.productId),
        userId: 100,
        username: 'user',
        nickname: '我',
        avatar: '',
        content: body.content,
        star: body.star,
        images: body.images || [],
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      }
      createdComments.unshift(comment)
      return {
        code: 200,
        message: '评论发表成功',
        data: comment.id,
        timestamp: Date.now(),
      }
    },
  },
  // 9.3 追加评论
  {
    url: '/api/comment/append',
    method: 'post',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '追评成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 9.4 商家回复
  {
    url: '/api/comment/reply',
    method: 'post',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '回复成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods