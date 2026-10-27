import service from '@/api'
import type { PageResult } from '@/types/common'

export interface AdminUser {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
  role: number
  status: number
  createdAt: string
}

export const getAdminUsers = (params: { page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<AdminUser>; timestamp: number }>('/admin/users', { params })
}

export const setUserStatus = (id: number, status: number) => {
  return service.put(`/admin/users/${id}/status`, { status })
}

export const getPendingMerchants = () => {
  return service.get('/admin/merchants/pending')
}

export const reviewMerchant = (id: number, data: { approved: boolean; reason?: string }) => {
  return service.put(`/admin/merchants/${id}/review`, data)
}

export const getAdminProducts = (params: { page?: number; size?: number; keyword?: string; status?: number }) => {
  return service.get('/admin/products', { params })
}

export const getAdminOrders = (params: { page?: number; size?: number; status?: number }) => {
  return service.get('/admin/orders', { params })
}

export const getAdminSalesReport = () => {
  return service.get('/admin/report/sales')
}