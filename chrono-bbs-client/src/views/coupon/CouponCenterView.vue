<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeckillList, getMyCoupons, claimSeckillCoupon } from '@/api/seckill'
import { CouponType, CouponStatus, UserCouponStatus, type SeckillCoupon, type UserCoupon } from '@/types/coupon'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref<'center' | 'mine'>('center')

const centerLoading = ref(false)
const coupons = ref<SeckillCoupon[]>([])

const mineLoading = ref(false)
const myCoupons = ref<UserCoupon[]>([])

const discountText = (discountType: CouponType, discountValue: number, minAmount: number) => {
  if (discountType === CouponType.FULL_REDUCTION) {
    return `满${(minAmount / 100).toFixed(0)}减${(discountValue / 100).toFixed(0)}`
  }
  return `${(discountValue / 10).toFixed(1)} 折`
}

const userCouponStatusMap: Record<number, { text: string; type: 'success' | 'info' | 'danger' }> = {
  [UserCouponStatus.UNUSED]: { text: '未使用', type: 'success' },
  [UserCouponStatus.USED]: { text: '已使用', type: 'info' },
  [UserCouponStatus.EXPIRED]: { text: '已过期', type: 'danger' },
}

const loadCenter = async () => {
  centerLoading.value = true
  try {
    const res = await getSeckillList({ page: 1, size: 50 })
    coupons.value = res.data.list || []
  } catch {
    coupons.value = []
  } finally {
    centerLoading.value = false
  }
}

const loadMine = async () => {
  mineLoading.value = true
  try {
    const res = await getMyCoupons({ page: 1, size: 50 })
    myCoupons.value = res.data.list || []
  } catch {
    myCoupons.value = []
  } finally {
    mineLoading.value = false
  }
}

const handleClaim = async (coupon: SeckillCoupon) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再领取优惠券')
    router.push({ path: '/login', query: { redirect: '/coupons' } })
    return
  }
  try {
    await claimSeckillCoupon(coupon.id)
    ElMessage.success(`已领取「${coupon.name}」`)
    loadCenter()
    loadMine()
  } catch {
    // 错误提示由拦截器统一处理
  }
}

onMounted(() => {
  loadCenter()
  loadMine()
})
</script>

<template>
  <div class="cc-page">
    <el-tabs v-model="activeTab" class="cc-tabs">
      <el-tab-pane label="领券中心" name="center">
        <div v-loading="centerLoading" class="cc-grid">
          <div v-for="coupon in coupons" :key="coupon.id" class="cc-card">
            <div class="cc-card-left">
              <p class="cc-name">{{ coupon.name }}</p>
              <p class="cc-desc">{{ coupon.description || '暂无描述' }}</p>
              <p class="cc-meta">剩余 {{ coupon.remainCount }}/{{ coupon.totalCount }} · 每人限领 {{ coupon.perUserLimit }} 张</p>
              <p class="cc-meta">{{ coupon.startTime }} ~ {{ coupon.endTime }}</p>
            </div>
            <div class="cc-card-right">
              <p class="cc-discount">{{ discountText(coupon.discountType, coupon.discountValue, coupon.minAmount) }}</p>
              <el-button
                type="danger"
                round
                size="small"
                :disabled="coupon.status !== CouponStatus.ACTIVE"
                @click="handleClaim(coupon)"
              >
                {{ coupon.status === CouponStatus.ACTIVE ? '立即领取' : coupon.status === CouponStatus.NOT_STARTED ? '未开始' : '已结束' }}
              </el-button>
            </div>
          </div>
          <div v-if="coupons.length === 0 && !centerLoading" class="cc-empty">暂无优惠券</div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的优惠券" name="mine">
        <div v-loading="mineLoading" class="cc-list">
          <div v-for="item in myCoupons" :key="item.id" class="cc-mine-row">
            <div class="cc-mine-info">
              <p class="cc-name">{{ item.couponName }}</p>
              <p class="cc-meta">领取时间：{{ item.claimTime }} · 有效期至 {{ item.endTime }}</p>
            </div>
            <div class="cc-mine-right">
              <p class="cc-discount">{{ discountText(item.discountType, item.discountValue, item.minAmount) }}</p>
              <el-tag :type="userCouponStatusMap[item.status]?.type || 'info'" size="small">
                {{ userCouponStatusMap[item.status]?.text || '未知' }}
              </el-tag>
            </div>
          </div>
          <div v-if="myCoupons.length === 0 && !mineLoading" class="cc-empty">暂无优惠券，快去领券中心看看吧</div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.cc-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cc-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.cc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.cc-card {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.cc-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.cc-card-left {
  min-width: 0;
  flex: 1;
}

.cc-name {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.cc-desc {
  margin: 0 0 8px;
  font-size: 12px;
  color: #999;
}

.cc-meta {
  margin: 0;
  font-size: 12px;
  color: #bbb;
  line-height: 1.6;
}

.cc-card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

.cc-discount {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #e2231a;
}

.cc-list {
  min-height: 120px;
}

.cc-mine-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.cc-mine-row:last-child {
  border-bottom: none;
}

.cc-mine-info {
  min-width: 0;
}

.cc-mine-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.cc-empty {
  text-align: center;
  padding: 48px 0;
  color: #999;
}
</style>
