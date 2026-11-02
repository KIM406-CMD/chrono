<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { getCartList } from '@/api/cart'
import { getAddressList } from '@/api/address'
import { createOrder, getSubmitToken } from '@/api/order'
import { getMyCoupons, calculateCoupon } from '@/api/seckill'
import type { CartItem } from '@/types/cart'
import type { Address } from '@/types/address'
import { CouponType, UserCouponStatus, type UserCoupon, type CalculateResult } from '@/types/coupon'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)

const cartItems = ref<CartItem[]>([])
const addresses = ref<Address[]>([])
const selectedAddress = ref<Address | null>(null)
const remark = ref('')
const submitToken = ref('')

// 优惠券
const availableCoupons = ref<UserCoupon[]>([])
const selectedCouponId = ref<number | null>(null)
const couponLoading = ref(false)
const calcResult = ref<CalculateResult | null>(null)

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const totalAmountFen = computed(() =>
  cartItems.value.reduce((s, i) => s + i.skuPrice * i.quantity, 0)
)
const totalAmountYuan = computed(() => priceYuan(totalAmountFen.value))
const payAmountFen = computed(() => calcResult.value?.finalAmount ?? totalAmountFen.value)
const payAmountYuan = computed(() => priceYuan(payAmountFen.value))
const discountAmountFen = computed(() => calcResult.value?.discountAmount ?? 0)
const discountAmountYuan = computed(() => priceYuan(discountAmountFen.value))

// 满足门槛的可用券（未使用且达到满减门槛）
const usableCoupons = computed(() =>
  availableCoupons.value.filter(c => c.minAmount <= totalAmountFen.value)
)

const discountText = (c: UserCoupon) => {
  if (c.discountType === CouponType.FULL_REDUCTION) {
    return `减 ¥${priceYuan(c.discountValue)}`
  }
  return `${(c.discountValue / 10).toFixed(1)} 折`
}
const minAmountText = (c: UserCoupon) =>
  c.minAmount > 0 ? `满 ¥${priceYuan(c.minAmount)} 可用` : '无门槛'

const itemCount = computed(() =>
  cartItems.value.reduce((s, i) => s + i.quantity, 0)
)

const hasSelectableAddress = computed(() => addresses.value.length > 0)

const loadData = async () => {
  loading.value = true
  try {
    const [cartRes, addrRes, tokenRes] = await Promise.all([
      getCartList(),
      getAddressList(),
      getSubmitToken(),
    ])
    cartItems.value = (cartRes.data || []).filter(i => i.selected === 1)
    addresses.value = addrRes.data || []
    submitToken.value = tokenRes.data.token

    const def = addresses.value.find(a => a.isDefault === 1)
    if (def) {
      selectedAddress.value = def
    } else if (addresses.value.length > 0) {
      selectedAddress.value = addresses.value[0]
    }

    if (cartItems.value.length === 0) {
      ElMessage.warning('没有选中商品，请返回购物车选择')
      router.replace('/cart')
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }

  loadCoupons()
}

// 加载可用优惠券（失败不影响下单主流程）
const loadCoupons = async () => {
  try {
    const res = await getMyCoupons({ page: 1, size: 50, status: UserCouponStatus.UNUSED })
    availableCoupons.value = (res.data.list || []).filter(c => c.status === UserCouponStatus.UNUSED)
  } catch {
    availableCoupons.value = []
  }
}

// 选择优惠券：不传则不用券，选中后调后端重算优惠
const selectCoupon = async (userCouponId: number | null) => {
  selectedCouponId.value = userCouponId
  if (userCouponId == null) {
    calcResult.value = null
    return
  }
  couponLoading.value = true
  try {
    const res = await calculateCoupon({
      totalAmount: totalAmountFen.value,
      userCouponId,
    })
    calcResult.value = res.data
  } catch {
    // 计算失败（如未达门槛）：回退为不使用券
    selectedCouponId.value = null
    calcResult.value = null
  } finally {
    couponLoading.value = false
  }
}

const selectAddress = (addr: Address) => {
  selectedAddress.value = addr
}

const handleSubmit = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('没有选中商品')
    return
  }

  submitting.value = true
  try {
    const res = await createOrder({
      token: submitToken.value,
      addressId: selectedAddress.value.id,
      cartItemIds: cartItems.value.map(i => i.id),
      remark: remark.value || undefined,
      userCouponId: selectedCouponId.value ?? undefined,
    })

    ElMessage.success('订单创建成功')
    router.replace(`/payment/${res.data.orderId}`)
  } catch {
    ElMessage.error('订单创建失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="checkout-page" v-loading="loading">
    <!-- 顶部 -->
    <div class="checkout-header">
      <el-button text class="back-btn" @click="router.push('/cart')">
        <ArrowLeft />
        <span>返回购物车</span>
      </el-button>
      <h2 class="checkout-title">确认订单</h2>
    </div>

    <div class="checkout-container">
      <!-- 左侧主体 -->
      <div class="checkout-main">
        <!-- 收货地址 -->
        <section class="section-card">
          <div class="section-head">
            <h3>收货地址</h3>
            <el-button text type="primary" size="small" :icon="Plus" @click="router.push('/address')">
              添加新地址
            </el-button>
          </div>

          <div v-if="!hasSelectableAddress" class="addr-empty">
            <p>暂无收货地址，请添加</p>
          </div>

          <div v-else class="addr-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="addr-item"
              :class="{ active: selectedAddress?.id === addr.id }"
              @click="selectAddress(addr)"
            >
              <div class="addr-check">
                <span class="check-dot" v-if="selectedAddress?.id === addr.id">✓</span>
              </div>
              <div class="addr-info">
                <p class="addr-contact">
                  <strong>{{ addr.receiverName }}</strong>
                  <span class="addr-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault === 1" size="small" type="danger" class="default-tag">
                    默认
                  </el-tag>
                </p>
                <p class="addr-full">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- 商品清单 -->
        <section class="section-card">
          <div class="section-head">
            <h3>商品清单</h3>
            <span class="section-count">共 {{ itemCount }} 件</span>
          </div>

          <div class="item-list">
            <div
              v-for="item in cartItems"
              :key="item.id"
              class="checkout-item"
            >
              <div class="item-img">
                <span class="img-placeholder">暂无图片</span>
              </div>
              <div class="item-detail">
                <p class="item-name">{{ item.productName }}</p>
                <p class="item-sku">{{ item.skuSpecText }} | {{ item.merchantName }}</p>
              </div>
              <div class="item-price">¥{{ priceYuan(item.skuPrice) }}</div>
              <div class="item-qty">×{{ item.quantity }}</div>
              <div class="item-subtotal">
                ¥{{ ((item.skuPrice * item.quantity) / 100).toFixed(2) }}
              </div>
            </div>
          </div>
        </section>

        <!-- 优惠券 -->
        <section class="section-card">
          <div class="section-head">
            <h3>优惠券</h3>
            <span class="section-count" v-if="usableCoupons.length > 0">可用 {{ usableCoupons.length }} 张</span>
          </div>

          <div v-if="availableCoupons.length === 0" class="coupon-empty">
            <p>暂无可用优惠券</p>
          </div>
          <div v-else-if="usableCoupons.length === 0" class="coupon-empty">
            <p>暂无满足当前订单门槛的优惠券</p>
          </div>
          <div v-else class="coupon-list" v-loading="couponLoading">
            <div
              class="coupon-item"
              :class="{ active: selectedCouponId === null }"
              @click="selectCoupon(null)"
            >
              <span class="coupon-tag">不</span>
              <div class="coupon-info">
                <p class="coupon-name">不使用优惠券</p>
              </div>
            </div>
            <div
              v-for="c in usableCoupons"
              :key="c.id"
              class="coupon-item"
              :class="{ active: selectedCouponId === c.id }"
              @click="selectCoupon(c.id)"
            >
              <span class="coupon-tag">券</span>
              <div class="coupon-info">
                <p class="coupon-name">{{ c.couponName }}</p>
                <p class="coupon-meta">{{ discountText(c) }} · {{ minAmountText(c) }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 订单备注 -->
        <section class="section-card">
          <div class="section-head">
            <h3>订单备注</h3>
          </div>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="选填：如有特殊要求请在此留言"
            maxlength="200"
            show-word-limit
          />
        </section>
      </div>

      <!-- 右侧结算栏 -->
      <aside class="checkout-sidebar">
        <div class="sidebar-card">
          <h4 class="sidebar-title">订单摘要</h4>

          <div class="summary-row">
            <span>商品总数</span>
            <span>{{ itemCount }} 件</span>
          </div>

          <div class="summary-row">
            <span>商品金额</span>
            <span class="price">¥{{ totalAmountYuan }}</span>
          </div>

          <div class="summary-row" v-if="discountAmountFen > 0">
            <span>优惠券</span>
            <span class="discount">-¥{{ discountAmountYuan }}</span>
          </div>

          <div class="summary-row">
            <span>运费</span>
            <span class="free">免运费</span>
          </div>

          <div class="summary-total">
            <span>应付总额</span>
            <span class="total-price">¥{{ payAmountYuan }}</span>
          </div>

          <el-button
            type="danger"
            size="large"
            class="submit-btn"
            :loading="submitting"
            :disabled="!selectedAddress || cartItems.length === 0"
            @click="handleSubmit"
          >
            提交订单
          </el-button>

          <p class="submit-hint">
            提交即表示您同意
            <a href="#">用户协议</a> 和 <a href="#">隐私政策</a>
          </p>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.checkout-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.checkout-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 14px;
  color: #666;
}

.back-btn:hover {
  color: #e2231a;
}

.checkout-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.checkout-container {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.checkout-main {
  flex: 1;
  min-width: 0;
}

/* ========== 卡片区 ========== */
.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-head h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.section-count {
  font-size: 13px;
  color: #999;
}

/* ========== 地址选择 ========== */
.addr-empty {
  text-align: center;
  padding: 30px;
  color: #999;
}

.addr-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.addr-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  width: calc(50% - 6px);
  padding: 14px;
  border: 2px solid #eee;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
  position: relative;
}

.addr-item:hover {
  border-color: #f5a3a3;
}

.addr-item.active {
  border-color: #e2231a;
}

.addr-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.addr-item.active .addr-check {
  border-color: #e2231a;
  background: #e2231a;
}

.check-dot {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.addr-info {
  flex: 1;
  min-width: 0;
}

.addr-contact {
  margin: 0 0 4px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.addr-phone {
  color: #999;
  font-size: 13px;
}

.default-tag {
  transform: scale(0.85);
}

.addr-full {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.addr-edit {
  flex-shrink: 0;
  color: #999;
}

/* ========== 商品清单 ========== */
.item-list {
  display: flex;
  flex-direction: column;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.checkout-item:last-child {
  border-bottom: none;
}

.item-img {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  border: 1px solid #eee;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.img-placeholder {
  font-size: 11px;
  color: #bbb;
}

.item-detail {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-sku {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.item-price {
  font-size: 14px;
  color: #666;
  width: 100px;
  text-align: right;
  flex-shrink: 0;
}

.item-qty {
  font-size: 14px;
  color: #999;
  width: 50px;
  text-align: center;
  flex-shrink: 0;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  width: 100px;
  text-align: right;
  flex-shrink: 0;
}

/* ========== 优惠券选择 ========== */
.coupon-empty {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}

.coupon-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.coupon-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: calc(50% - 6px);
  padding: 12px 14px;
  border: 2px solid #eee;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.coupon-item:hover {
  border-color: #f5a3a3;
}

.coupon-item.active {
  border-color: #e2231a;
  background: #fff7f6;
}

.coupon-tag {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e2231a;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.coupon-info {
  min-width: 0;
}

.coupon-name {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.coupon-meta {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* ========== 右侧结算栏 ========== */
.checkout-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.sidebar-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 80px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.price {
  color: #333;
  font-weight: 500;
}

.free {
  color: #67c23a;
}

.discount {
  color: #e2231a;
  font-weight: 600;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #e2231a;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  margin-top: 8px;
}

.submit-hint {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin: 12px 0 0;
}

.submit-hint a {
  color: #409eff;
  text-decoration: none;
}
</style>