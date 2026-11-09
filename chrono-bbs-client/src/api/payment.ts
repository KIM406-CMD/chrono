import service from '@/api'
import type { PageResult } from '@/types/common'

export interface PaymentRecord {
  id: number
  orderId: number
  orderNo: string
  payMethod: number
  cardNo: string
  amount: number
  status: number
  paidAt: string | null
  createdAt: string
}

// 卡号支付
export const payByCard = (data: { orderId: number; cardNo: string; expireDate: string; cvv: string }) => {
  return service.post('/payment/card', data)
}

// 获取支付二维码
export const getPaymentQRCode = (orderId: number) => {
  return service.post<any, { code: number; message: string; data: { orderId: number; totalAmount: number; qrcodeUrl: string }; timestamp: number }>('/payment/qrcode', { orderId })
}

// 确认二维码支付
export const confirmQRCodePayment = (orderId: number) => {
  return service.post('/payment/confirm-qrcode', { orderId })
}

// 我的支付记录（Mock 扩展，正式后端未实现）
export const getPaymentRecords = (params: { page?: number; size?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<PaymentRecord>; timestamp: number }>(
    '/payment/records',
    { params },
  )
}