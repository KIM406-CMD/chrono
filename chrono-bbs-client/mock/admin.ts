import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

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
      const keyword = String(query.keyword || '').trim().toLowerCase()
      const status = query.status !== undefined && query.status !== '' ? parseInt(query.status) : null
      const categoryId = query.categoryId ? parseInt(query.categoryId) : null
      const merchantId = query.merchantId ? parseInt(query.merchantId) : null

      let list = allProducts.map((product) => ({
        id: product.id,
        name: product.name,
        images: product.images,
        brand: product.brand,
        categoryId: product.categoryId,
        merchantId: product.merchantId,
        merchantName: product.merchantName,
        minPrice: product.minPrice,
        sales: product.sales,
        status: product.status,
        createdAt: product.createdAt,
        updatedAt: product.updatedAt,
      }))

      if (keyword) {
        list = list.filter((item) =>
          item.name.toLowerCase().includes(keyword)
          || (item.brand || '').toLowerCase().includes(keyword),
        )
      }
      if (status !== null) list = list.filter((item) => item.status === status)
      if (categoryId !== null) list = list.filter((item) => item.categoryId === categoryId)
      if (merchantId !== null) list = list.filter((item) => item.merchantId === merchantId)

      const total = list.length
      const start = (page - 1) * size
      const end = start + size

      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page,
          size,
          list: list.slice(start, end),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 12.7 管理员 - 商品上下架（Mock 扩展）
  {
    url: '/api/admin/products/:id/status',
    method: 'put',
    timeout: 300,
    response: ({ query, body }) => {
      const product = allProducts.find((item) => item.id === parseInt(query.id))
      if (product) product.status = Number(body.status)
      return {
        code: 200,
        message: body.status === 1 ? '上架成功' : '下架成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 12.8 管理员 - 删除商品（Mock 扩展）
  {
    url: '/api/admin/products/:id',
    method: 'delete',
    timeout: 300,
    response: ({ query }) => {
      const index = allProducts.findIndex((item) => item.id === parseInt(query.id))
      if (index >= 0) allProducts.splice(index, 1)
      return {
        code: 200,
        message: '商品删除成功',
        data: null,
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
          avgOrderAmount: 8230,
          topProducts: [
            { productId: 1, productName: '华为 智能手机 型号1', salesCount: 320, totalAmount: 320000 },
            { productId: 2, productName: '小米 智能手机 型号2', salesCount: 260, totalAmount: 260000 },
            { productId: 3, productName: '苹果 笔记本电脑 型号3', salesCount: 180, totalAmount: 180000 },
            { productId: 4, productName: '耐克 T恤 型号4', salesCount: 150, totalAmount: 150000 },
            { productId: 5, productName: '三只松鼠 坚果炒货 型号5', salesCount: 120, totalAmount: 120000 },
          ],
          salesByCategory: [
            { categoryId: 1, categoryName: '食品饮料', totalAmount: 420000 },
            { categoryId: 2, categoryName: '生鲜果蔬', totalAmount: 310000 },
            { categoryId: 3, categoryName: '美妆个护', totalAmount: 260000 },
            { categoryId: 4, categoryName: '数码家电', totalAmount: 180000 },
            { categoryId: 5, categoryName: '服饰鞋包', totalAmount: 64567 },
          ],
          salesByMerchant: [
            { merchantId: 1, shopName: '官方旗舰店1', totalAmount: 520000 },
            { merchantId: 2, shopName: '官方旗舰店2', totalAmount: 380000 },
            { merchantId: 3, shopName: '官方旗舰店3', totalAmount: 234567 },
            { merchantId: 4, shopName: '官方旗舰店4', totalAmount: 100000 },
          ],
          salesTrend: [
            { date: '2025-01-01', amount: 120000 },
            { date: '2025-01-02', amount: 168000 },
            { date: '2025-01-03', amount: 142000 },
            { date: '2025-01-04', amount: 210000 },
            { date: '2025-01-05', amount: 189000 },
            { date: '2025-01-06', amount: 226000 },
            { date: '2025-01-07', amount: 179567 },
          ],
          orderTrend: [
            { date: '2025-01-01', createdCount: 18, paidCount: 15, completedCount: 12 },
            { date: '2025-01-02', createdCount: 24, paidCount: 21, completedCount: 17 },
            { date: '2025-01-03', createdCount: 20, paidCount: 18, completedCount: 14 },
            { date: '2025-01-04', createdCount: 30, paidCount: 26, completedCount: 22 },
            { date: '2025-01-05', createdCount: 27, paidCount: 24, completedCount: 19 },
            { date: '2025-01-06', createdCount: 32, paidCount: 29, completedCount: 25 },
            { date: '2025-01-07', createdCount: 25, paidCount: 22, completedCount: 18 },
          ],
          orderStatusDistribution: {
            待支付: 20,
            已支付: 30,
            已发货: 25,
            已完成: 60,
            已取消: 10,
            退款中: 3,
            已退款: 2,
          },
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