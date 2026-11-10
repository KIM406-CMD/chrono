import service from '@/api'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  CaptchaResponse,
  ForgotPasswordRequest,
  ForgotCodeRequest,
  ForgotCodeResponse,
  UpdateProfileRequest,
  UpdatePasswordRequest,
  UserInfo,
} from '@/types/user'

// 获取图形验证码
export const getCaptcha = () => {
  return service.get<any, { code: number; message: string; data: CaptchaResponse; timestamp: number }>('/user/captcha')
}

// 用户注册
export const register = (data: RegisterRequest) => {
  return service.post<any, { code: number; message: string; data: LoginResponse; timestamp: number }>('/user/register', data)
}

// 用户登录
export const login = (data: LoginRequest) => {
  return service.post<any, { code: number; message: string; data: LoginResponse; timestamp: number }>('/user/login', data)
}

// 获取当前用户信息
export const getUserInfo = () => {
  return service.get<any, { code: number; message: string; data: UserInfo; timestamp: number }>('/user/info')
}

// 修改个人信息
export const updateProfile = (data: UpdateProfileRequest) => {
  return service.put('/user/profile', data)
}

// 修改密码
export const updatePassword = (data: UpdatePasswordRequest) => {
  return service.put('/user/password', data)
}

// 退出登录
export const logout = () => {
  return service.post('/user/logout')
}

// 发送重置密码验证码
export const sendForgotCode = (data: ForgotCodeRequest) => {
  return service.post<any, { code: number; message: string; data: ForgotCodeResponse; timestamp: number }>('/user/forgot-code', data)
}

// 忘记密码 - 重置密码
export const resetPassword = (data: ForgotPasswordRequest) => {
  return service.post('/user/reset-password', data)
}