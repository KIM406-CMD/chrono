import service from '@/api'
import type { PageResult } from '@/types/common'
import type { RefundItem } from './refund'

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

export interface AdminMerchant {
  id: number
  userId: number
  applicantUsername: string
  shopName: string
  contactPhone: string
  status: number
  reviewReason: string | null
  reviewedAt: string | null
  createdAt: string
}

export const getMerchants = (params?: { status?: number; page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<AdminMerchant>; timestamp: number }>('/admin/merchants', { params })
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

// 管理员退款审核列表（Mock 扩展，正式后端未实现）
export const getAdminRefunds = (params: { page?: number; size?: number; status?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<RefundItem>; timestamp: number }>(
    '/admin/refunds',
    { params },
  )
}

// 管理员商品上下架（Mock 扩展，正式后端未实现）
export const setAdminProductStatus = (id: number, status: number) => {
  return service.put(`/admin/products/${id}/status`, { status })
}

// 管理员删除商品（Mock 扩展，正式后端未实现）
export const deleteAdminProduct = (id: number) => {
  return service.delete(`/admin/products/${id}`)
}