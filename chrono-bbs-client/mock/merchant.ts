import { MockMethod } from 'vite-plugin-mock'

const mockMethods: MockMethod[] = [
  // 11.1 商家入驻申请
  {
    url: '/api/merchant/apply',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      return {
        code: 200,
        message: '入驻申请已提交，等待管理员审核',
        data: {
          merchantId: 100,
          status: 0, // 0=待审核
        },
        timestamp: Date.now(),
      }
    },
  },
  // 11.2 获取商家信息
  {
    url: '/api/merchant/info',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          userId: 100,
          shopName: '测试店铺',
          businessLicense: '',
          contactPhone: '13800138000',
          status: 1,
          reviewReason: null,
          reviewedAt: '2025-01-01 10:00:00',
          createdAt: '2025-01-01 09:00:00',
          updatedAt: '2025-01-01 10:00:00',
        },
        timestamp: Date.now(),
      }
    },
  },
  // 11.3 商家商品列表
  {
    url: '/api/merchant/products',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const mockProducts = Array.from({ length: 15 }, (_, i) => ({
        id: i + 1,
        name: `商品${i + 1}`,
        images: [],
        minPrice: (i + 1) * 1000,
        sales: Math.floor(Math.random() * 500),
        status: i % 5 === 0 ? 0 : 1,
        createdAt: '2025-01-01 10:00:00',
      }))
      const total = mockProducts.length
      const start = (page - 1) * size
      return {
        code: 200,
        message: 'success',
        data: { total, page, size, list: mockProducts.slice(start, start + size) },
        timestamp: Date.now(),
      }
    },
  },
  // 11.4 商家订单列表
  {
    url: '/api/merchant/orders',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      const mockOrders = Array.from({ length: 10 }, (_, i) => ({
        id: 1001 + i,
        orderNo: `20250101${String(i).padStart(6, '0')}`,
        userId: (i % 5) + 1,
        username: `user${(i % 5) + 1}`,
        totalAmount: (i + 1) * 2000,
        status: (i % 5) + 1,
        createdAt: '2025-01-01 10:00:00',
      }))
      const total = mockOrders.length
      const start = (page - 1) * size
      return {
        code: 200,
        message: 'success',
        data: { total, page, size, list: mockOrders.slice(start, start + size) },
        timestamp: Date.now(),
      }
    },
  },
  // 11.5 发货
  {
    url: '/api/merchant/ship/:orderId',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '发货成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 11.6 商家销售统计
  {
    url: '/api/merchant/stats',
    method: 'get',
    timeout: 500,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          totalSales: 5000000,
          orderCount: 120,
          topProducts: [
            { productId: 1, productName: '商品1', salesCount: 50, totalAmount: 500000 },
            { productId: 2, productName: '商品2', salesCount: 30, totalAmount: 300000 },
          ],
          salesByCategory: [
            { categoryId: 1, categoryName: '分类1', totalAmount: 2000000 },
          ],
          salesTrend: Array.from({ length: 7 }, (_, i) => ({
            date: `2025-01-${String(i + 1).padStart(2, '0')}`,
            amount: Math.floor(Math.random() * 100000),
          })),
        },
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods