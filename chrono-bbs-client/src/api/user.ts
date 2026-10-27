import request from './index'
import type { LoginParams, LoginResult, RegisterParams, UserInfo } from '@/types/user'
import type { Result } from '@/types/common'

export function login(data: LoginParams) {
  return request.post<Result<LoginResult>>('/auth/login', data)
}

export function register(data: RegisterParams) {
  return request.post<Result<void>>('/auth/register', data)
}

export function getUserInfo() {
  return request.get<Result<UserInfo>>('/auth/info')
}

export function logout() {
  return request.post<Result<void>>('/auth/logout')
}
