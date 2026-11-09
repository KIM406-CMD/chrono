import service from '@/api'
import type { PageResult } from '@/types/common'

export interface RefundItem {
  id: number
  orderId: number
  orderItemId: number
  userId: number
  productId: number
  productName: string
  type: number
  reason: string
  amount: number
  status: number
  reviewerId: number | null
  reviewReason: string | null
  reviewedAt: string | null
  createdAt: string
}

export const applyRefund = (data: { orderItemId: number; type: number; reason: string; amount: number }) => {
  return service.post<any, { code: number; message: string; data: number; timestamp: number }>('/refund', data)
}

// 退款列表（我的）
export const getRefundList = (params: { page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<RefundItem>; timestamp: number }>('/refund/my', { params })
}

export const getRefundDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: RefundItem; timestamp: number }>(`/refund/${id}`)
}

export const reviewRefund = (id: number, data: { approved: number; reviewReason?: string }) => {
  return service.put(`/refund/${id}/review`, data)
}