import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

function getProductInfo(productId: number) {
  const p = allProducts.find(item => item.id === productId)
  if (!p) {
    return { name: `商品${productId}`, price: productId * 1000 }
  }
  return { name: p.name, price: p.minPrice }
}

export default [
  // 10.1 申请退款/退货
  {
    url: '/api/refund',
    method: 'post',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '退款申请已提交，等待审核',
        data: {
          refundId: 1001,
          status: 0, // 0=待审核
        },
        timestamp: Date.now(),
      }
    },
  },
  // 10.2 退款列表
  {
    url: '/api/refund/list',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      
      const mockRefunds = [
        {
          id: 1001,
          orderId: 1001,
          orderItemId: 10001,
          userId: 100,
          type: 1, // 1=退款
          reason: '商品质量问题',
          amount: 2000,
          status: 0, // 0=待审核
          reviewerId: null,
          reviewReason: null,
          reviewedAt: null,
          createdAt: '2025-01-01 10:00:00',
        },
        {
          id: 1002,
          orderId: 1002,
          orderItemId: 10002,
          userId: 100,
          type: 2, // 2=退货
          reason: '不想要了',
          amount: 3000,
          status: 1, // 1=已通过
          reviewerId: 1,
          reviewReason: '审核通过',
          reviewedAt: '2025-01-01 11:00:00',
          createdAt: '2025-01-01 09:00:00',
        },
      ]
      
      const total = mockRefunds.length
      const start = (page - 1) * size
      const end = start + size
      const list = mockRefunds.slice(start, end)
      
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
  // 10.3 退款详情
  {
    url: '/api/refund/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const refundId = parseInt(query.id)
      
      return {
        code: 200,
        message: 'success',
        data: {
          id: refundId,
          orderId: 1001,
          orderNo: '202501011000001001',
          orderItemId: 10001,
          userId: 100,
          username: 'user',
          type: 1,
          reason: '商品质量问题',
          amount: 2000,
          status: 0,
          reviewerId: null,
          reviewReason: null,
          reviewedAt: null,
          createdAt: '2025-01-01 10:00:00',
          // 订单项信息
          orderItem: {
            id: 10001,
            productName: getProductInfo(1).name,
            productImage: '',
            skuSpecText: '默认规格',
            quantity: 2,
            unitPrice: getProductInfo(1).price,
            subtotal: getProductInfo(1).price * 2,
          },
        },
        timestamp: Date.now(),
      }
    },
  },
  // 10.4 商家/管理员审核退款
  {
    url: '/api/refund/:id/review',
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
] as MockMethod[]