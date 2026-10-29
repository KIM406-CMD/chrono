import service from '@/api'
import type { PageResult } from '@/types/common'

export interface MerchantInfo {
  id: number
  userId: number
  shopName: string
  businessLicense: string
  contactPhone: string
  status: number
  reviewReason: string | null
  reviewedAt: string | null
  createdAt: string
  updatedAt: string
}

export const applyMerchant = (data: { shopName: string; contactPhone: string; businessLicense?: string }) => {
  return service.post('/merchant/apply', data)
}

export const getMerchantInfo = () => {
  return service.get<any, { code: number; message: string; data: MerchantInfo; timestamp: number }>('/merchant/info')
}

export const updateMerchantInfo = (data: { shopName?: string; contactPhone?: string }) => {
  return service.put('/merchant/info', data)
}

// 11.3 商家商品列表
export const getMerchantProducts = (params?: { page?: number; size?: number; status?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<any>; timestamp: number }>('/merchant/products', { params })
}

// 11.4 商家订单列表
export const getMerchantOrders = (params?: { page?: number; size?: number; status?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<any>; timestamp: number }>('/merchant/orders', { params })
}

// 11.5 发货
export const shipOrder = (orderId: number) => {
  return service.put('/merchant/ship/' + orderId)
}

// 11.6 商家销售统计
export const getMerchantStats = (params?: { startDate?: string; endDate?: string }) => {
  return service.get<any, { code: number; message: string; data: any; timestamp: number }>('/merchant/stats', { params })
}