// 通用返回格式
export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页返回格式
export interface PageResult<T = any> {
  total: number
  page: number
  size: number
  list: T[]
}

// 角色
export const Role = {
  USER: 1,
  MERCHANT: 2,
  ADMIN: 3,
} as const
export type Role = (typeof Role)[keyof typeof Role]

// 订单状态
export const OrderStatus = {
  PENDING_PAYMENT: 1, // 待支付
  PAID: 2,            // 已支付（待发货）
  SHIPPED: 3,         // 已发货（待收货）
  COMPLETED: 4,       // 已完成
  CANCELLED: 5,       // 已取消
  REFUNDING: 6,       // 退款中
  REFUNDED: 7,        // 已退款
} as const
export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus]

// 商品状态
export const ProductStatus = {
  OFF_SHELF: 0, // 下架
  ON_SHELF: 1,  // 上架
} as const
export type ProductStatus = (typeof ProductStatus)[keyof typeof ProductStatus]