import service from '@/api'
import type { PageResult } from '@/types/common'

export interface RefundItem {
  id: number
  orderId: number
  orderItemId: number
  userId: number
  merchantId?: number
  productId: number
  productName: string
  type: number
  reason: string
  amount: number
  /** 0=待审核 1=已通过 2=已拒绝 3=待寄回 4=待商家收货 5=退货完成 */
  status: number
  reviewerId: number | null
  reviewReason: string | null
  reviewedAt: string | null
  returnCompany?: string | null
  returnLogisticsNo?: string | null
  returnedAt?: string | null
  returnReceivedAt?: string | null
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

// 买家填写退货物流
export const submitRefundReturn = (
  id: number,
  data: { returnCompany: string; returnLogisticsNo: string },
) => {
  return service.put(`/refund/${id}/return`, data)
}

// 商家/管理员确认收到退货
export const confirmRefundReturnReceived = (id: number) => {
  return service.put(`/refund/${id}/return-received`)
}