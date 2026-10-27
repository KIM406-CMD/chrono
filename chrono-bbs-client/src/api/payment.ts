import service from '@/api'

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