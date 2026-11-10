import service from '@/api'
import type { PageResult } from '@/types/common'
import type {
  SeckillCoupon,
  UserCoupon,
  CouponQueryParams,
  CouponFormData,
  ClaimResult,
  CalculateParams,
  CalculateResult,
} from '@/types/coupon'

// 秒杀券列表
export const getSeckillList = (params?: CouponQueryParams) => {
  return service.get<any, { code: number; message: string; data: PageResult<SeckillCoupon>; timestamp: number }>('/seckill/list', { params })
}

// 秒杀券详情
export const getSeckillDetail = (id: number) => {
  return service.get<any, { code: number; message: string; data: SeckillCoupon; timestamp: number }>(`/seckill/detail/${id}`)
}

// 抢领秒杀券
export const claimSeckillCoupon = (couponId: number) => {
  return service.post<any, { code: number; message: string; data: ClaimResult; timestamp: number }>(`/seckill/claim/${couponId}`)
}

// 我的优惠券
export const getMyCoupons = (params?: CouponQueryParams) => {
  return service.get<any, { code: number; message: string; data: PageResult<UserCoupon>; timestamp: number }>('/seckill/my-coupons', { params })
}

// 创建秒杀券
export const createSeckillCoupon = (data: CouponFormData) => {
  return service.post('/seckill/create', data)
}

// 编辑秒杀券
export const updateSeckillCoupon = (id: number, data: CouponFormData) => {
  return service.put(`/seckill/update/${id}`, data)
}

// 删除秒杀券
export const deleteSeckillCoupon = (id: number) => {
  return service.delete(`/seckill/${id}`)
}

// 下单优惠计算
export const calculateCoupon = (data: CalculateParams) => {
  return service.post<any, { code: number; message: string; data: CalculateResult; timestamp: number }>('/seckill/calculate', data)
}
