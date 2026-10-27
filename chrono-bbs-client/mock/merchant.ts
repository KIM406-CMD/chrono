import { MockMethod } from 'vite-plugin-mock'

export default [
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
          businessLicense: '', // 营业执照URL为空
          contactPhone: '13800138000',
          status: 1, // 1=已通过
          reviewReason: null,
          reviewedAt: '2025-01-01 10:00:00',
          createdAt: '2025-01-01 09:00:00',
          updatedAt: '2025-01-01 10:00:00',
        },
        timestamp: Date.now(),
      }
    },
  },
  // 11.3 商家修改信息
  {
    url: '/api/merchant/info',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '修改成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
] as MockMethod[]