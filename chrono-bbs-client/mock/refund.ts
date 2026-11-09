import { MockMethod } from 'vite-plugin-mock'
import { allProducts } from './product'

const productName = (id: number) =>
  allProducts.find((item) => item.id === id)?.name || `商品${id}`

const nowText = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

// 共享退款数据：用户端、商家端、管理员端共用，审核后状态会同步变化
export const mockRefunds: any[] = [
  {
    id: 1001,
    orderId: 1001,
    orderItemId: 10001,
    userId: 100,
    merchantId: 1,
    productId: 1,
    productName: productName(1),
    type: 1,
    reason: '商品质量问题',
    amount: 2000,
    status: 0,
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
    merchantId: 1,
    productId: 2,
    productName: productName(2),
    type: 2,
    reason: '尺码不合适，申请退货',
    amount: 3000,
    status: 0,
    reviewerId: null,
    reviewReason: null,
    reviewedAt: null,
    createdAt: '2025-01-02 09:00:00',
  },
  {
    id: 1003,
    orderId: 1003,
    orderItemId: 10003,
    userId: 101,
    merchantId: 2,
    productId: 3,
    productName: productName(3),
    type: 1,
    reason: '不想要了',
    amount: 1500,
    status: 1,
    reviewerId: 1,
    reviewReason: '审核通过',
    reviewedAt: '2025-01-03 11:00:00',
    createdAt: '2025-01-03 09:00:00',
  },
  {
    id: 1004,
    orderId: 1004,
    orderItemId: 10004,
    userId: 102,
    merchantId: 1,
    productId: 4,
    productName: productName(4),
    type: 1,
    reason: '商品与描述不符',
    amount: 2500,
    status: 2,
    reviewerId: 1,
    reviewReason: '商品已拆封使用，不符合退款条件',
    reviewedAt: '2025-01-04 11:00:00',
    createdAt: '2025-01-04 09:00:00',
  },
]

const pageResult = (list: any[], query: Record<string, any>) => {
  const page = parseInt(query.page) || 1
  const size = parseInt(query.size) || 10
  const status = query.status !== undefined && query.status !== '' ? parseInt(query.status) : null
  const filtered = status === null ? list : list.filter((item) => item.status === status)
  return {
    total: filtered.length,
    page,
    size,
    list: filtered.slice((page - 1) * size, page * size),
  }
}

const mockMethods: MockMethod[] = [
  // 10.1 申请退款/退货
  {
    url: '/api/refund',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const id = 2000 + mockRefunds.length + 1
      mockRefunds.unshift({
        id,
        orderId: 0,
        orderItemId: Number(body.orderItemId) || 0,
        userId: 100,
        merchantId: 1,
        productId: 0,
        productName: '申请中的商品',
        type: Number(body.type) || 1,
        reason: body.reason || '',
        amount: Number(body.amount) || 0,
        status: 0,
        reviewerId: null,
        reviewReason: null,
        reviewedAt: null,
        createdAt: nowText(),
      })
      return {
        code: 200,
        message: '退款申请已提交，等待审核',
        data: id,
        timestamp: Date.now(),
      }
    },
  },
  // 10.2 退款列表（我的）
  {
    url: '/api/refund/my',
    method: 'get',
    timeout: 300,
    response: ({ query }) => ({
      code: 200,
      message: 'success',
      data: pageResult(mockRefunds.filter((item) => item.userId === 100), query),
      timestamp: Date.now(),
    }),
  },
  // 10.5 退款列表（商家，Mock 扩展）
  {
    url: '/api/merchant/refunds',
    method: 'get',
    timeout: 300,
    response: ({ query }) => ({
      code: 200,
      message: 'success',
      data: pageResult(mockRefunds.filter((item) => item.merchantId === 1), query),
      timestamp: Date.now(),
    }),
  },
  // 10.6 退款列表（管理员，Mock 扩展）
  {
    url: '/api/admin/refunds',
    method: 'get',
    timeout: 300,
    response: ({ query }) => ({
      code: 200,
      message: 'success',
      data: pageResult(mockRefunds, query),
      timestamp: Date.now(),
    }),
  },
  // 10.3 退款详情
  {
    url: '/api/refund/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const refundId = parseInt(query.id)
      const refund = mockRefunds.find((item) => item.id === refundId)
      if (!refund) {
        return { code: 404, message: '退款单不存在', data: null, timestamp: Date.now() }
      }
      return {
        code: 200,
        message: 'success',
        data: {
          ...refund,
          orderNo: `20250101100000${refund.orderId}`,
          username: 'user',
          orderItem: {
            id: refund.orderItemId,
            productName: refund.productName,
            productImage: '',
            skuSpecText: '默认规格',
            quantity: 1,
            unitPrice: refund.amount,
            subtotal: refund.amount,
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
    response: ({ query, body }) => {
      const refund = mockRefunds.find((item) => item.id === parseInt(query.id))
      if (refund) {
        refund.status = Number(body.approved) === 1 ? 1 : 2
        refund.reviewerId = 1
        refund.reviewReason = body.reviewReason || (refund.status === 1 ? '审核通过' : '审核拒绝')
        refund.reviewedAt = nowText()
      }
      return {
        code: 200,
        message: '审核成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods
