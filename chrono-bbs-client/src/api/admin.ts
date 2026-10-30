import service from '@/api'
import type { PageResult } from '@/types/common'
import type { MerchantInfo } from '@/api/merchant'

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

export const getAdminUsers = (params: { page?: number; size?: number; keyword?: string; role?: number; status?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<AdminUser>; timestamp: number }>('/admin/users', { params })
}

export const setUserStatus = (id: number, status: number) => {
  return service.put(`/admin/users/${id}/status`, { status })
}

export const getMerchants = (params?: { status?: number; page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<MerchantInfo>; timestamp: number }>('/admin/merchants', { params })
}

// 兼容旧调用：默认查询待审核商家
export const getPendingMerchants = () => {
  return getMerchants({ status: 0 })
}

export const reviewMerchant = (id: number, data: { approved: number; reviewReason?: string }) => {
  return service.put(`/admin/merchants/${id}/review`, data)
}

export const createCategory = (data: {
  name: string
  parentId?: number
  level: number
  sortOrder?: number
  icon?: string
}) => {
  return service.post('/admin/categories', data)
}

export const updateCategory = (
  id: number,
  data: { name?: string; parentId?: number; level?: number; sortOrder?: number; icon?: string },
) => {
  return service.put(`/admin/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return service.delete(`/admin/categories/${id}`)
}

export const getAdminProducts = (params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  merchantId?: number
  status?: number
}) => {
  return service.get('/admin/products', { params })
}

export const getAdminOrders = (params: { page?: number; size?: number; orderNo?: string; status?: number }) => {
  return service.get('/admin/orders', { params })
}

export const getAdminSalesReport = (params?: { startDate?: string; endDate?: string; groupBy?: 'day' | 'week' | 'month' }) => {
  return service.get('/admin/report', { params })
}