import request from '@/utils/request'
import type { LoginParams, LoginResult, RegisterParams, Result, UserInfo } from '@/types/api'

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