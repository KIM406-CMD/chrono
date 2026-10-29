import { MockMethod } from 'vite-plugin-mock'

const mockMethods: MockMethod[] = [
  // 12.1 管理员 - 用户列表
  {
    url: '/api/admin/users',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      
      const mockUsers = Array.from({ length: 20 }, (_, i) => ({
        id: i + 1,
        username: `user${i + 1}`,
        nickname: `用户${i + 1}`,
        phone: `138${String(i).padStart(8, '0')}`,
        email: `user${i + 1}@example.com`,
        avatar: '',
        role: i === 0 ? 3 : 1, // 第一个是管理员
        status: i % 5 === 0 ? 1 : 0, // 每5个禁用1个
        createdAt: '2025-01-01 10:00:00',
      }))
      
      const total = mockUsers.length
      const start = (page - 1) * size
      const end = start + size
      const list = mockUsers.slice(start, end)
      
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
  // 12.2 管理员 - 禁用/启用用户
  {
    url: '/api/admin/users/:id/status',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '操作成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 12.3 管理员 - 商家审核列表
  {
    url: '/api/admin/merchants',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: [
          {
            id: 1,
            userId: 101,
            username: 'merchant1',
            shopName: '店铺1',
            businessLicense: '',
            contactPhone: '13800138001',
            status: 0, // 待审核
            createdAt: '2025-01-01 10:00:00',
          },
          {
            id: 2,
            userId: 102,
            username: 'merchant2',
            shopName: '店铺2',
            businessLicense: '',
            contactPhone: '13800138002',
            status: 0,
            createdAt: '2025-01-01 11:00:00',
          },
        ],
        timestamp: Date.now(),
      }
    },
  },
  // 12.4 管理员 - 审核商家
  {
    url: '/api/admin/merchants/:id/review',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '审核成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 12.5 管理员 - 全平台商品列表
  {
    url: '/api/admin/products',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      
      const mockProducts = Array.from({ length: 50 }, (_, i) => ({
        id: i + 1,
        name: `商品${i + 1}`,
        images: [],
        brand: `品牌${i % 5 + 1}`,
        categoryId: (i % 8) + 1,
        merchantId: (i % 5) + 1,
        merchantName: `店铺${(i % 5) + 1}`,
        minPrice: (i + 1) * 1000,
        sales: Math.floor(Math.random() * 1000),
        status: i % 10 === 0 ? 0 : 1,
        createdAt: '2025-01-01 10:00:00',
      }))
      
      const total = mockProducts.length
      const start = (page - 1) * size
      const end = start + size
      const list = mockProducts.slice(start, end)
      
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
  // 12.6 管理员 - 全平台订单列表
  {
    url: '/api/admin/orders',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      
      const mockOrders = Array.from({ length: 30 }, (_, i) => ({
        id: i + 1001,
        orderNo: `20250101${String(i).padStart(6, '0')}`,
        userId: (i % 10) + 1,
        username: `user${(i % 10) + 1}`,
        totalAmount: (i + 1) * 1000,
        status: (i % 5) + 1,
        createdAt: '2025-01-01 10:00:00',
      }))
      
      const total = mockOrders.length
      const start = (page - 1) * size
      const end = start + size
      const list = mockOrders.slice(start, end)
      
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
  // 12.7 管理员 - 销售报表
  {
    url: '/api/admin/report',
    method: 'get',
    timeout: 500,
    response: ({ query }) => {
      return {
        code: 200,
        message: 'success',
        data: {
          totalSales: 1234567,
          totalOrders: 150,
          totalUsers: 80,
          totalProducts: 50,
          dailySales: Array.from({ length: 7 }, (_, i) => ({
            date: `2025-01-${String(i + 1).padStart(2, '0')}`,
            amount: Math.floor(Math.random() * 100000),
            orderCount: Math.floor(Math.random() * 20),
          })),
          topProducts: Array.from({ length: 5 }, (_, i) => ({
            id: i + 1,
            name: `商品${i + 1}`,
            sales: 100 - i * 10,
            amount: (100 - i * 10) * 1000,
          })),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 12.8 管理员 - 新增分类
  {
    url: '/api/admin/categories',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      return {
        code: 200,
        message: '分类创建成功',
        data: { id: 100 },
        timestamp: Date.now(),
      }
    },
  },
  // 12.9 管理员 - 修改分类
  {
    url: '/api/admin/categories/:id',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '分类修改成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 12.10 管理员 - 删除分类
  {
    url: '/api/admin/categories/:id',
    method: 'delete',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '分类删除成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods