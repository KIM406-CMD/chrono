import { MockMethod } from 'vite-plugin-mock'
import Mock from 'mockjs'

const mockMethods: MockMethod[] = [
  // 2.1 获取图形验证码
  {
    url: '/api/user/captcha',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          uuid: Mock.Random.guid(),
          image: '', // 模拟验证码图片，前端显示为空
        },
        timestamp: Date.now(),
      }
    },
  },
  // 2.2 用户注册
  {
    url: '/api/user/register',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      const { username, role } = body
      return {
        code: 200,
        message: '注册成功',
        data: {
          token: Mock.Random.guid(),
          userInfo: {
            id: Mock.Random.integer(100, 999),
            username,
            nickname: username,
            phone: body.phone || '',
            role, // 1=普通用户, 2=商家
          },
        },
        timestamp: Date.now(),
      }
    },
  },
  // 2.3 用户登录
  {
    url: '/api/user/login',
    method: 'post',
    timeout: 500,
    response: ({ body }) => {
      const { username } = body
      // 模拟不同角色的用户
      const roleMap: Record<string, number> = {
        admin: 3,
        merchant: 2,
        user: 1,
      }
      const role = roleMap[username] || 1
      
      return {
        code: 200,
        message: '登录成功',
        data: {
          token: Mock.Random.guid(),
          userInfo: {
            id: Mock.Random.integer(100, 999),
            username,
            nickname: username === 'admin' ? '管理员' : username === 'merchant' ? '测试商家' : '测试用户',
            phone: '138****8000',
            role, // 1=普通用户, 2=商家, 3=管理员
          },
        },
        timestamp: Date.now(),
      }
    },
  },
  // 2.4 获取当前用户信息
  {
    url: '/api/user/info',
    method: 'get',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          id: 100,
          username: 'user',
          nickname: '测试用户',
          phone: '13800138000',
          email: 'user@example.com',
          avatar: '', // 图片为空
          role: 1, // 1=普通用户
          status: 1, // 1=正常
        },
        timestamp: Date.now(),
      }
    },
  },
  // 2.5 修改个人信息
  {
    url: '/api/user/profile',
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
  // 2.6 修改密码
  {
    url: '/api/user/password',
    method: 'put',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '密码修改成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 2.7 退出登录
  {
    url: '/api/user/logout',
    method: 'post',
    timeout: 300,
    response: () => {
      return {
        code: 200,
        message: '退出成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  // 2.8 发送重置密码验证码
  {
    url: '/api/user/forgot-code',
    method: 'post',
    timeout: 500,
    response: () => {
      return {
        code: 200,
        message: '验证码已发送',
        data: {
          uuid: Mock.Random.guid(),
          maskAccount: '138****8000',
        },
        timestamp: Date.now(),
      }
    },
  },
  // 2.9 忘记密码 - 重置密码
  {
    url: '/api/user/reset-password',
    method: 'post',
    timeout: 500,
    response: () => {
      return {
        code: 200,
        message: '密码重置成功，请重新登录',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods