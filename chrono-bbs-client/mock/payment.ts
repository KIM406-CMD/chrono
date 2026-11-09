import { MockMethod } from 'vite-plugin-mock'

const paymentRecords = [
  { id: 1, orderId: 1001, orderNo: '202601011200001001', payMethod: 1, cardNo: '**** **** **** 1234', amount: 10000, status: 1, paidAt: '2026-01-01 12:05:00', createdAt: '2026-01-01 12:04:30' },
  { id: 2, orderId: 1002, orderNo: '202601021200001002', payMethod: 2, cardNo: '', amount: 5000, status: 1, paidAt: '2026-01-02 09:10:00', createdAt: '2026-01-02 09:09:20' },
  { id: 3, orderId: 1003, orderNo: '202601031200001003', payMethod: 1, cardNo: '**** **** **** 5678', amount: 23600, status: 1, paidAt: '2026-01-03 18:30:00', createdAt: '2026-01-03 18:29:10' },
  { id: 4, orderId: 1004, orderNo: '202601041200001004', payMethod: 2, cardNo: '', amount: 8800, status: 2, paidAt: null, createdAt: '2026-01-04 11:20:00' },
  { id: 5, orderId: 1005, orderNo: '202601051200001005', payMethod: 1, cardNo: '**** **** **** 9012', amount: 15000, status: 1, paidAt: '2026-01-05 20:15:00', createdAt: '2026-01-05 20:14:00' },
]

const mockMethods: MockMethod[] = [
  // 7.4 我的支付记录（Mock 扩展）
  {
    url: '/api/payment/records',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const page = parseInt(query.page) || 1
      const size = parseInt(query.size) || 10
      return {
        code: 200,
        message: 'success',
        data: {
          total: paymentRecords.length,
          page,
          size,
          list: paymentRecords.slice((page - 1) * size, page * size),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 7.1 卡号支付
  {
    url: '/api/payment/card',
    method: 'post',
    timeout: 1000, // 模拟支付处理时间
    response: ({ body }) => {
      const { orderId, cardNo, expireDate, cvv } = body
      
      // 模拟支付成功
      return {
        code: 200,
        message: '支付成功',
        data: {
          status: 2, // 已支付
          paidAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        },
        timestamp: Date.now(),
      }
    },
  },
  // 7.2 获取支付二维码
  {
    url: '/api/payment/qrcode',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const { orderId } = body
      
      return {
        code: 200,
        message: 'success',
        data: {
          orderId,
          totalAmount: 5000, // 模拟金额
          qrcodeUrl: '', // 二维码URL为空
        },
        timestamp: Date.now(),
      }
    },
  },
  // 7.3 确认二维码支付
  {
    url: '/api/payment/confirm-qrcode',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      const { orderId } = body
      
      return {
        code: 200,
        message: '支付成功',
        data: {
          status: 2, // 已支付
          paidAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        },
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods