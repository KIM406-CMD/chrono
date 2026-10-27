import { MockMethod } from 'vite-plugin-mock'

export default [
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
] as MockMethod[]