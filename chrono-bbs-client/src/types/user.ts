import { Role } from './common'

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
  role: Role
  status: number
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
  captchaUuid: string
  captchaCode: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: {
    id: number
    username: string
    nickname: string
    phone: string
    role: Role
  }
}

// 注册请求
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  phone: string
  role: Role
  captchaUuid: string
  captchaCode: string
}

// 验证码响应
export interface CaptchaResponse {
  uuid: string
  image: string
}

// 发送重置密码验证码请求
export interface ForgotCodeRequest {
  account: string
  captchaUuid: string
  captchaCode: string
}

// 发送重置密码验证码响应
export interface ForgotCodeResponse {
  uuid: string
  maskAccount: string
}

// 重置密码请求
export interface ForgotPasswordRequest {
  uuid: string
  code: string
  newPassword: string
  confirmPassword: string
}

// 修改个人信息请求
export interface UpdateProfileRequest {
  nickname?: string
  email?: string
  avatar?: string
}

// 修改密码请求
export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}