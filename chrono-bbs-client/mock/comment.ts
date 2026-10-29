import { MockMethod } from 'vite-plugin-mock'

const mockMethods: MockMethod[] = [
  // 9.1 商品评论列表
  {
    url: '/api/comment/list/:productId',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const productId = parseInt(query.productId)
      
      const mockComments = Array.from({ length: 20 }, (_, i) => ({
        id: i + 1,
        productId,
        userId: (i % 10) + 1,
        username: `user${(i % 10) + 1}`,
        nickname: `用户${(i % 10) + 1}`,
        avatar: '',
        content: `这是第${i + 1}条评论，商品质量很好，物流也很快。`,
        rating: 5 - (i % 3), // 3-5星
        images: [],
        createdAt: '2025-01-01 10:00:00',
      }))
      
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
    response: () => {
      return {
        code: 200,
        message: '评论发表成功',
        data: null,
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