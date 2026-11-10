// 收货地址
export interface Address {
  id: number
  userId: number
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number  // 1=默认, 0=非默认
  createdAt: string
  updatedAt: string
}

// 新增地址请求
export interface CreateAddressRequest {
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: number
}

// 修改地址请求
export interface UpdateAddressRequest {
  id: number
  receiverName?: string
  phone?: string
  province?: string
  city?: string
  district?: string
  detail?: string
  isDefault?: number
}