import type { OrderStatus } from './common'

// 订单项
export interface OrderItem {
  id: number
  orderId: number
  productId: number
  skuId: number
  quantity: number
  unitPrice: number
  subtotal: number
  productName: string
  productImage: string
  skuSpecText: string
}

// 订单
export interface Order {
  id: number
  orderNo: string
  userId: number
  addressSnapshot: string  // JSON字符串
  totalAmount: number
  status: OrderStatus
  cancelReason: string | null
  paidAt: string | null
  shippedAt: string | null
  receivedAt: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
  items: OrderItem[]
}

// 订单列表项（简化版）
export interface OrderListItem {
  id: number
  orderNo: string
  totalAmount: number
  status: OrderStatus
  createdAt: string
  itemCount: number
  firstItemName: string
  firstItemImage: string
}

// 创建订单请求
export interface CreateOrderRequest {
  addressId: number
  cartItemIds: number[]
  remark?: string
}

// 创建订单响应
export interface CreateOrderResponse {
  orderId: number
  orderNo: string
  totalAmount: number
  status: OrderStatus
  createdAt: string
}

// 订单查询参数
export interface OrderQueryParams {
  page?: number
  size?: number
  status?: OrderStatus
}