export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export type UserRole = 'USER' | 'MERCHANT' | 'ADMIN'

export interface UserInfo {
  id: number
  username: string
  role: UserRole
  avatar?: string
  phone?: string
  email?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export interface RegisterParams {
  username: string
  password: string
  phone?: string
  email?: string
}