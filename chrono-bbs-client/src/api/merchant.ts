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