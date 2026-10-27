import { MockMethod } from 'vite-plugin-mock'
import Mock from 'mockjs'

export default [
  // 13.1 文件上传
  {
    url: '/api/upload',
    method: 'post',
    timeout: 500,
    response: () => {
      return {
        code: 200,
        message: '上传成功',
        data: {
          url: '', // 图片URL为空
        },
        timestamp: Date.now(),
      }
    },
  },
] as MockMethod[]