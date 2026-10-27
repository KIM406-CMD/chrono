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

// 错误码
export enum ErrorCode {
  SUCCESS = 200,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  SERVER_ERROR = 500,
}

// 角色枚举
export enum Role {
  USER = 1,
  MERCHANT = 2,
  ADMIN = 3,
}

// 订单状态
export enum OrderStatus {
  PENDING_PAYMENT = 1,    // 待支付
  PAID = 2,               // 已支付（待发货）
  SHIPPED = 3,            // 已发货（待收货）
  COMPLETED = 4,          // 已完成
  CANCELLED = 5,          // 已取消
  REFUNDING = 6,          // 退款中
  REFUNDED = 7,           // 已退款
}

// 支付方式
export enum PayMethod {
  CARD = 1,      // 卡号支付
  QRCODE = 2,    // 二维码支付
}

// 商品状态
export enum ProductStatus {
  OFF_SHELF = 0,  // 下架
  ON_SHELF = 1,   // 上架
}

// 商家审核状态
export enum MerchantStatus {
  PENDING = 0,    // 待审核
  APPROVED = 1,   // 已通过
  REJECTED = 2,   // 已拒绝
}