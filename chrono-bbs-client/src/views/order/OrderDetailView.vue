<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderDetail, cancelOrder, receiveOrder, deleteOrder } from '@/api/order'
import { payByCard } from '@/api/payment'
import type { Order } from '@/types/order'
import { OrderStatus } from '@/types/common'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => Number(route.params.id))
const order = ref<Order | null>(null)
const loading = ref(false)
const canceling = ref(false)

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const addressInfo = computed(() => {
  if (!order.value?.addressSnapshot) return null
  try {
    return JSON.parse(order.value.addressSnapshot)
  } catch {
    return null
  }
})

const steps = computed(() => {
  if (!order.value) return []
  const s = order.value.status
  return [
    { title: '提交订单', desc: order.value.createdAt, done: true },
    {
      title: '支付成功',
      desc: order.value.paidAt || '',
      done: s >= OrderStatus.PAID,
      pending: s === OrderStatus.PENDING_PAYMENT,
    },
    {
      title: '商家发货',
      desc: order.value.shippedAt || '',
      done: s >= OrderStatus.SHIPPED,
      pending: s === OrderStatus.PAID,
    },
    {
      title: '确认收货',
      desc: order.value.receivedAt || '',
      done: s >= OrderStatus.COMPLETED,
      pending: s === OrderStatus.SHIPPED,
    },
    {
      title: s === OrderStatus.CANCELLED ? '订单已取消' : '已完成',
      desc: s === OrderStatus.CANCELLED
        ? (order.value.cancelReason || '')
        : (order.value.completedAt || ''),
      done: s === OrderStatus.COMPLETED,
      canceled: s === OrderStatus.CANCELLED,
    },
  ]
})

const statusLabel = computed(() => {
  if (!order.value) return ''
  switch (order.value.status) {
    case OrderStatus.PENDING_PAYMENT: return '待付款'
    case OrderStatus.PAID: return '待发货'
    case OrderStatus.SHIPPED: return '待收货'
    case OrderStatus.COMPLETED: return '已完成'
    case OrderStatus.CANCELLED: return '已取消'
    default: return ''
  }
})

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data
  } catch {
    ElMessage.error('订单加载失败')
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  router.push(`/payment/${orderId.value}`)
}

const handleCancel = async () => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '选填',
    })
  } catch {
    return
  }
  canceling.value = true
  try {
    await cancelOrder(orderId.value)
    ElMessage.success('订单已取消')
    loadOrder()
  } catch {
    ElMessage.error('取消失败')
  } finally {
    canceling.value = false
  }
}

const handleReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch {
    return
  }
  try {
    await receiveOrder(orderId.value)
    ElMessage.success('已确认收货')
    loadOrder()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '删除订单', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteOrder(orderId.value)
    ElMessage.success('订单已删除')
    router.replace('/orders')
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="od-header">
      <el-button text class="back-btn" @click="router.push('/orders')">
        <ArrowLeft />
        <span>返回订单列表</span>
      </el-button>
      <h2 class="od-title">订单详情</h2>
    </div>

    <template v-if="order">
      <!-- 状态栏 -->
      <div class="od-status-bar">
        <div class="status-label">
          {{ statusLabel }}
        </div>
        <div v-if="order.status === OrderStatus.PENDING_PAYMENT" class="status-hint">
          订单已提交，请尽快完成支付
        </div>
        <div v-else-if="order.status === OrderStatus.PAID" class="status-hint">
          支付成功，等待商家发货
        </div>
        <div v-else-if="order.status === OrderStatus.SHIPPED" class="status-hint">
          商品已发货，请留意收货
        </div>
        <div v-else-if="order.status === OrderStatus.COMPLETED" class="status-hint status-ok">
          订单已完成，感谢您的购买
        </div>
        <div v-else-if="order.status === OrderStatus.CANCELLED" class="status-hint status-cancel">
          订单已取消
        </div>
      </div>

      <!-- 物流进度 -->
      <div class="od-section">
        <h3>订单进度</h3>
        <div class="od-steps">
          <div
            v-for="(step, i) in steps"
            :key="i"
            class="od-step"
            :class="{
              done: step.done,
              pending: step.pending,
              canceled: step.canceled,
            }"
          >
            <div class="step-dot">
              <span v-if="step.done">✓</span>
              <span v-else-if="step.canceled">✕</span>
            </div>
            <div class="step-info">
              <p class="step-title">{{ step.title }}</p>
              <p class="step-desc" v-if="step.desc">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="od-section" v-if="addressInfo">
        <h3>收货地址</h3>
        <div class="od-address">
          <p class="addr-contact">
            <strong>{{ addressInfo.receiverName }}</strong>
            <span class="addr-phone">{{ addressInfo.phone }}</span>
          </p>
          <p class="addr-full">
            {{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }}
            {{ addressInfo.detail }}
          </p>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="od-section">
        <h3>商品清单</h3>
        <div class="od-items">
          <div
            v-for="item in order.items"
            :key="item.id"
            class="od-item"
          >
            <div class="od-item-img">
              <span class="img-placeholder">暂无图片</span>
            </div>
            <div class="od-item-info">
              <p class="od-item-name">{{ item.productName }}</p>
              <p class="od-item-sku">{{ item.skuSpecText }}</p>
            </div>
            <div class="od-item-price">
              ¥{{ priceYuan(item.unitPrice) }}
            </div>
            <div class="od-item-qty">×{{ item.quantity }}</div>
            <div class="od-item-sub">
              ¥{{ priceYuan(item.subtotal) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="od-section">
        <h3>订单信息</h3>
        <div class="od-info-list">
          <div class="od-info-row">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ order.orderNo }}</span>
          </div>
          <div class="od-info-row">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ order.createdAt }}</span>
          </div>
          <div class="od-info-row" v-if="order.paidAt">
            <span class="info-label">付款时间</span>
            <span class="info-value">{{ order.paidAt }}</span>
          </div>
          <div class="od-info-row" v-if="order.shippedAt">
            <span class="info-label">发货时间</span>
            <span class="info-value">{{ order.shippedAt }}</span>
          </div>
          <div class="od-info-row" v-if="order.receivedAt">
            <span class="info-label">收货时间</span>
            <span class="info-value">{{ order.receivedAt }}</span>
          </div>
          <div class="od-info-row" v-if="order.completedAt">
            <span class="info-label">完成时间</span>
            <span class="info-value">{{ order.completedAt }}</span>
          </div>
        </div>
      </div>

      <!-- 金额汇总 -->
      <div class="od-total-bar">
        <div class="total-row">
          <span>商品总额</span>
          <span>¥{{ priceYuan(order.totalAmount) }}</span>
        </div>
        <div class="total-row">
          <span>运费</span>
          <span class="free">免运费</span>
        </div>
        <div class="total-row total-final">
          <span>实付金额</span>
          <span class="final-price">¥{{ priceYuan(order.totalAmount) }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="od-actions" v-if="order.status !== OrderStatus.COMPLETED && order.status !== OrderStatus.CANCELLED">
        <template v-if="order.status === OrderStatus.PENDING_PAYMENT">
          <el-button @click="handleCancel" :loading="canceling">取消订单</el-button>
          <el-button type="danger" @click="handlePay">立即付款</el-button>
        </template>
        <template v-else-if="order.status === OrderStatus.SHIPPED">
          <el-button type="primary" @click="handleReceive">确认收货</el-button>
        </template>
      </div>
      <div class="od-actions" v-else>
        <el-button type="danger" text @click="handleDelete">删除订单</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.order-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.od-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 14px;
  color: #666;
}
.back-btn:hover { color: #e2231a; }

.od-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

/* ========== 状态栏 ========== */
.od-status-bar {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-label {
  font-size: 20px;
  font-weight: 700;
  color: #e2231a;
}

.status-hint {
  font-size: 14px;
  color: #999;
}
.status-ok { color: #67c23a; }
.status-cancel { color: #c0c4cc; }

/* ========== Section ========== */
.od-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.od-section h3 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

/* ========== 进度 ========== */
.od-steps {
  display: flex;
  justify-content: space-between;
}

.od-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
  position: relative;
}

.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #e0e0e0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #ccc;
  position: relative;
  z-index: 1;
}

.od-step.done .step-dot {
  border-color: #e2231a;
  background: #e2231a;
  color: #fff;
}

.od-step.pending .step-dot {
  border-color: #e2231a;
  color: #e2231a;
  animation: pulse 1.5s infinite;
}

.od-step.canceled .step-dot {
  border-color: #c0c4cc;
  background: #c0c4cc;
  color: #fff;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(226,35,26,0.3); }
  50% { box-shadow: 0 0 0 6px rgba(226,35,26,0); }
}

.step-info {
  margin-top: 8px;
}

.step-title {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.od-step.done .step-title { color: #333; font-weight: 600; }
.od-step.pending .step-title { color: #e2231a; font-weight: 600; }

.step-desc {
  font-size: 11px;
  color: #bbb;
  margin: 2px 0 0;
}

/* ========== 地址 ========== */
.od-address {
  font-size: 14px;
}

.addr-contact {
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.addr-phone { color: #999; }

.addr-full {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* ========== 商品 ========== */
.od-items {
  display: flex;
  flex-direction: column;
}

.od-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.od-item:last-child { border-bottom: none; }

.od-item-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  border: 1px solid #eee;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.img-placeholder {
  font-size: 10px;
  color: #bbb;
}

.od-item-info {
  flex: 1;
  min-width: 0;
}

.od-item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.od-item-sku {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.od-item-price {
  font-size: 14px;
  color: #666;
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

.od-item-qty {
  font-size: 13px;
  color: #999;
  width: 40px;
  text-align: center;
  flex-shrink: 0;
}

.od-item-sub {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

/* ========== 订单信息 ========== */
.od-info-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.od-info-row {
  display: flex;
  padding: 8px 0;
  font-size: 13px;
}

.info-label {
  width: 90px;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  color: #333;
}

/* ========== 金额汇总 ========== */
.od-total-bar {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  padding: 4px 0;
  font-size: 14px;
  color: #666;
}

.free { color: #67c23a; }

.total-final {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 16px;
  color: #333;
}

.final-price {
  font-size: 22px;
  font-weight: 700;
  color: #e2231a;
}

/* ========== 操作 ========== */
.od-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0;
}
</style>