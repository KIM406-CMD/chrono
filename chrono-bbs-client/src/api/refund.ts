import service from '@/api'
import type { PageResult } from '@/types/common'

export interface RefundItem {
  id: number
  orderId: number
  orderItemId: number
  userId: number
  type: number
  reason: string
  amount: number
  status: number
  reviewerId: number | null
  reviewReason: string | null
  reviewedAt: string | null
  createdAt: string
}

export interface RefundDetail extends RefundItem {
  orderNo: string
  username: string
  orderItem: {
    id: number
    productName: string
    productImage: string
    skuSpecText: string
    quantity: number
    unitPrice: number
    subtotal: number
  }
}

export const getRefundList = (params: { page?: number; size?: number; status?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<RefundItem>; timestamp: number }>('/refund/list', { params })
}

export const getRefundDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: RefundDetail; timestamp: number }>(`/refund/${id}`)
}

export const reviewRefund = (id: number, data: { approved: boolean; reason?: string }) => {
  return service.put(`/refund/${id}/review`, data)
}