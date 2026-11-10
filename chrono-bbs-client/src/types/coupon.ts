// 券类型
export const CouponType = {
  FULL_REDUCTION: 1, // 满减：discountValue 单位为分
  DISCOUNT: 2,       // 折扣：discountValue 为折扣百分比（85 表示 8.5 折）
} as const
export type CouponType = (typeof CouponType)[keyof typeof CouponType]

// 券状态
export const CouponStatus = {
  NOT_STARTED: 0, // 未开始
  ACTIVE: 1,      // 进行中
  ENDED: 2,       // 已结束
} as const
export type CouponStatus = (typeof CouponStatus)[keyof typeof CouponStatus]

// 用户券状态
export const UserCouponStatus = {
  UNUSED: 0,  // 未使用
  USED: 1,    // 已使用
  EXPIRED: 2, // 已过期
} as const
export type UserCouponStatus = (typeof UserCouponStatus)[keyof typeof UserCouponStatus]

// 秒杀券
export interface SeckillCoupon {
  id: number
  name: string
  description: string
  discountType: CouponType
  discountValue: number
  minAmount: number
  totalCount: number
  remainCount: number
  startTime: string
  endTime: string
  perUserLimit: number
  status: CouponStatus
}

// 我的优惠券（my-coupons 返回的富化结构）
export interface UserCoupon {
  id: number
  couponId: number
  couponName: string
  discountType: CouponType
  discountValue: number
  minAmount: number
  status: UserCouponStatus
  endTime: string
  claimTime: string
}

// 券列表查询参数
export interface CouponQueryParams {
  page?: number
  size?: number
  status?: number
}

// 创建/编辑券请求
export interface CouponFormData {
  name: string
  description?: string
  discountType: number
  discountValue: number
  minAmount: number
  totalCount: number
  startTime: string
  endTime: string
  perUserLimit?: number
}

// 抢券结果
export interface ClaimResult {
  userCouponId: number
  couponName: string
  discountType: number
  discountValue: number
  minAmount: number
  endTime: string
}

// 下单优惠计算
export interface CalculateParams {
  userCouponId?: number
  totalAmount: number
}

export interface CalculateResult {
  originalAmount: number
  discountAmount: number
  finalAmount: number
  couponName: string | null
}
