import { MockMethod } from 'vite-plugin-mock'
import Mock from 'mockjs'

let seckillCoupons = [
  {
    id: 1,
    name: '新人满减券',
    description: '新用户专享，满50减10元',
    discountType: 1,
    discountValue: 1000,
    minAmount: 5000,
    totalCount: 1000,
    remainCount: 987,
    startTime: '2024-01-01 00:00:00',
    endTime: '2026-12-31 23:59:59',
    perUserLimit: 1,
    status: 1,
  },
  {
    id: 2,
    name: '双十一折扣券',
    description: '双十一狂欢，全场8.5折',
    discountType: 2,
    discountValue: 85,
    minAmount: 10000,
    totalCount: 500,
    remainCount: 320,
    startTime: '2024-11-01 00:00:00',
    endTime: '2026-12-31 23:59:59',
    perUserLimit: 1,
    status: 1,
  },
  {
    id: 3,
    name: '周末满减券',
    description: '周末特惠，满100减20',
    discountType: 1,
    discountValue: 2000,
    minAmount: 10000,
    totalCount: 300,
    remainCount: 300,
    startTime: '2026-01-01 00:00:00',
    endTime: '2026-12-31 23:59:59',
    perUserLimit: 2,
    status: 0,
  },
  {
    id: 4,
    name: '会员折扣券',
    description: '会员专享，全场9折',
    discountType: 2,
    discountValue: 90,
    minAmount: 0,
    totalCount: 2000,
    remainCount: 1890,
    startTime: '2024-06-01 00:00:00',
    endTime: '2026-12-31 23:59:59',
    perUserLimit: 3,
    status: 1,
  },
  {
    id: 5,
    name: '秒杀满减券',
    description: '限时秒杀，满30减8',
    discountType: 1,
    discountValue: 800,
    minAmount: 3000,
    totalCount: 100,
    remainCount: 100,
    startTime: '2026-10-28 10:00:00',
    endTime: '2026-10-28 12:00:00',
    perUserLimit: 1,
    status: 0,
  },
]

let userCoupons = [
  {
    id: 1,
    userId: 100,
    couponId: 1,
    status: 0,
    usedOrderId: null,
    claimTime: '2026-10-28 10:05:00',
    usedTime: null,
    createdAt: '2026-10-28 10:05:00',
  },
  {
    id: 2,
    userId: 100,
    couponId: 4,
    status: 0,
    usedOrderId: null,
    claimTime: '2026-10-27 15:30:00',
    usedTime: null,
    createdAt: '2026-10-27 15:30:00',
  },
  {
    id: 3,
    userId: 100,
    couponId: 2,
    status: 1,
    usedOrderId: 1001,
    claimTime: '2026-10-25 09:00:00',
    usedTime: '2026-10-25 14:20:00',
    createdAt: '2026-10-25 09:00:00',
  },
  {
    id: 4,
    userId: 100,
    couponId: 3,
    status: 2,
    usedOrderId: null,
    claimTime: '2026-09-15 12:00:00',
    usedTime: null,
    createdAt: '2026-09-15 12:00:00',
  },
  {
    id: 5,
    userId: 100,
    couponId: 5,
    status: 0,
    usedOrderId: null,
    claimTime: '2026-10-28 08:00:00',
    usedTime: null,
    createdAt: '2026-10-28 08:00:00',
  },
]

let nextUserCouponId = 6

function getCouponInfo(couponId: number) {
  return seckillCoupons.find(c => c.id === couponId)
}

const mockMethods: MockMethod[] = [
  {
    url: '/api/seckill/list',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const { status, page = 1, size = 10 } = query
      let list = seckillCoupons.filter(c => c.status !== 2)
      if (status !== undefined && status !== '') {
        list = list.filter(c => c.status === Number(status))
      }
      const total = list.length
      const start = (Number(page) - 1) * Number(size)
      const pagedList = list.slice(start, start + Number(size))

      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page: Number(page),
          size: Number(size),
          list: pagedList,
        },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/detail/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const id = Number(query.id)
      const coupon = seckillCoupons.find(c => c.id === id)

      if (!coupon) {
        return {
          code: 7001,
          message: '秒杀券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      return {
        code: 200,
        message: 'success',
        data: coupon,
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/claim/:couponId',
    method: 'post',
    timeout: 500,
    response: ({ query }) => {
      const couponId = Number(query.couponId)
      const coupon = seckillCoupons.find(c => c.id === couponId)

      if (!coupon) {
        return {
          code: 7001,
          message: '秒杀券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      if (coupon.status !== 1) {
        return {
          code: 7004,
          message: '秒杀券活动未开始或已结束',
          data: null,
          timestamp: Date.now(),
        }
      }

      const now = new Date()
      const startTime = new Date(coupon.startTime)
      const endTime = new Date(coupon.endTime)
      if (now < startTime || now > endTime) {
        return {
          code: 7004,
          message: '秒杀券活动未开始或已结束',
          data: null,
          timestamp: Date.now(),
        }
      }

      if (coupon.remainCount <= 0) {
        return {
          code: 7002,
          message: '秒杀券已被抢光',
          data: null,
          timestamp: Date.now(),
        }
      }

      const alreadyClaimed = userCoupons.filter(
        uc => uc.userId === 100 && uc.couponId === couponId,
      )
      if (alreadyClaimed.length >= coupon.perUserLimit) {
        return {
          code: 7003,
          message: '已领取过该秒杀券',
          data: null,
          timestamp: Date.now(),
        }
      }

      coupon.remainCount -= 1

      const userCoupon = {
        id: nextUserCouponId++,
        userId: 100,
        couponId: coupon.id,
        status: 0,
        usedOrderId: null,
        claimTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        usedTime: null,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      }
      userCoupons.push(userCoupon)

      return {
        code: 200,
        message: '领取成功',
        data: {
          userCouponId: userCoupon.id,
          couponName: coupon.name,
          discountType: coupon.discountType,
          discountValue: coupon.discountValue,
          minAmount: coupon.minAmount,
          endTime: coupon.endTime,
        },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/my-coupons',
    method: 'get',
    timeout: 300,
    response: ({ query }) => {
      const { status, page = 1, size = 10 } = query
      let list = userCoupons.filter(uc => uc.userId === 100)

      if (status !== undefined && status !== '') {
        list = list.filter(uc => uc.status === Number(status))
      }

      const total = list.length
      const start = (Number(page) - 1) * Number(size)
      const pagedList = list.slice(start, start + Number(size))

      const enrichedList = pagedList.map(uc => {
        const coupon = getCouponInfo(uc.couponId)
        return {
          id: uc.id,
          couponId: uc.couponId,
          couponName: coupon ? coupon.name : '未知券',
          discountType: coupon ? coupon.discountType : 0,
          discountValue: coupon ? coupon.discountValue : 0,
          minAmount: coupon ? coupon.minAmount : 0,
          status: uc.status,
          endTime: coupon ? coupon.endTime : '',
          claimTime: uc.claimTime,
        }
      })

      return {
        code: 200,
        message: 'success',
        data: {
          total,
          page: Number(page),
          size: Number(size),
          list: enrichedList,
        },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/create',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const {
        name,
        description,
        discountType,
        discountValue,
        minAmount,
        totalCount,
        startTime,
        endTime,
        perUserLimit = 1,
      } = body

      const newCoupon = {
        id: seckillCoupons.length + 1,
        name,
        description: description || '',
        discountType,
        discountValue,
        minAmount,
        totalCount,
        remainCount: totalCount,
        startTime,
        endTime,
        perUserLimit,
        status: 0,
      }
      seckillCoupons.push(newCoupon)

      return {
        code: 200,
        message: '创建成功',
        data: { id: newCoupon.id },
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/update/:id',
    method: 'put',
    timeout: 300,
    response: ({ query, body }) => {
      const id = Number(query.id)
      const coupon = seckillCoupons.find(c => c.id === id)

      if (!coupon) {
        return {
          code: 7001,
          message: '秒杀券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      const oldTotalCount = coupon.totalCount
      Object.assign(coupon, body)
      if (body.totalCount && body.totalCount > oldTotalCount) {
        coupon.remainCount += body.totalCount - oldTotalCount
      }

      return {
        code: 200,
        message: '更新成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/:id',
    method: 'delete',
    timeout: 300,
    response: ({ query }) => {
      const id = Number(query.id)
      const idx = seckillCoupons.findIndex(c => c.id === id)

      if (idx === -1) {
        return {
          code: 7001,
          message: '秒杀券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      const coupon = seckillCoupons[idx]
      if (coupon.status !== 0) {
        return {
          code: 400,
          message: '仅未开始状态的券可删除',
          data: null,
          timestamp: Date.now(),
        }
      }

      seckillCoupons.splice(idx, 1)

      return {
        code: 200,
        message: '删除成功',
        data: null,
        timestamp: Date.now(),
      }
    },
  },
  {
    url: '/api/seckill/calculate',
    method: 'post',
    timeout: 300,
    response: ({ body }) => {
      const { userCouponId, totalAmount } = body

      if (!userCouponId) {
        return {
          code: 200,
          message: 'success',
          data: {
            originalAmount: totalAmount,
            discountAmount: 0,
            finalAmount: totalAmount,
            couponName: null,
          },
          timestamp: Date.now(),
        }
      }

      const uc = userCoupons.find(
        u => u.id === Number(userCouponId) && u.userId === 100,
      )

      if (!uc) {
        return {
          code: 7001,
          message: '优惠券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      if (uc.status !== 0) {
        if (uc.status === 1) {
          return {
            code: 7006,
            message: '优惠券已使用',
            data: null,
            timestamp: Date.now(),
          }
        }
        return {
          code: 7006,
          message: '优惠券已过期',
          data: null,
          timestamp: Date.now(),
        }
      }

      const coupon = getCouponInfo(uc.couponId)
      if (!coupon) {
        return {
          code: 7001,
          message: '秒杀券不存在',
          data: null,
          timestamp: Date.now(),
        }
      }

      if (totalAmount < coupon.minAmount) {
        return {
          code: 7005,
          message: '不满足最低消费门槛',
          data: null,
          timestamp: Date.now(),
        }
      }

      let discountAmount = 0
      if (coupon.discountType === 1) {
        discountAmount = coupon.discountValue
      } else {
        discountAmount = Math.floor((totalAmount * (100 - coupon.discountValue)) / 100)
      }

      const finalAmount = Math.max(0, totalAmount - discountAmount)

      return {
        code: 200,
        message: 'success',
        data: {
          originalAmount: totalAmount,
          discountAmount,
          finalAmount,
          couponName: coupon.name,
        },
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods