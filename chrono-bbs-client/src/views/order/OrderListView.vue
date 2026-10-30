<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderList, cancelOrder, receiveOrder, deleteOrder } from '@/api/order'
import type { OrderListItem } from '@/types/order'
import { OrderStatus } from '@/types/common'

const router = useRouter()
const loading = ref(false)
const orders = ref<OrderListItem[]>([])
const activeTab = ref<number | 'all'>('all')
const page = ref(1)
const total = ref(0)
const size = 10

const tabs = [
  { label: '全部订单', value: 'all' as const },
  { label: '待付款', value: OrderStatus.PENDING_PAYMENT },
  { label: '待发货', value: OrderStatus.PAID },
  { label: '待收货', value: OrderStatus.SHIPPED },
  { label: '已完成', value: OrderStatus.COMPLETED },
  { label: '已取消', value: OrderStatus.CANCELLED },
]

const statusLabel = (status: OrderStatus) => {
  switch (status) {
    case OrderStatus.PENDING_PAYMENT: return '待付款'
    case OrderStatus.PAID: return '待发货'
    case OrderStatus.SHIPPED: return '待收货'
    case OrderStatus.COMPLETED: return '已完成'
    case OrderStatus.CANCELLED: return '已取消'
    default: return '未知'
  }
}

const statusClass = (status: OrderStatus) => {
  switch (status) {
    case OrderStatus.PENDING_PAYMENT: return 's-pending'
    case OrderStatus.PAID: return 's-paid'
    case OrderStatus.SHIPPED: return 's-shipped'
    case OrderStatus.COMPLETED: return 's-done'
    case OrderStatus.CANCELLED: return 's-cancel'
    default: return ''
  }
}

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadOrders = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; status?: OrderStatus } = { page: page.value, size }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value as OrderStatus
    }
    const res = await getOrderList(params)
    orders.value = res.data.list || []
    total.value = res.data.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = (tab: number | 'all') => {
  activeTab.value = tab
  page.value = 1
  loadOrders()
}

const handleViewDetail = (order: OrderListItem) => {
  router.push(`/order/${order.id}`)
}

const handlePay = (order: OrderListItem) => {
  router.push(`/payment/${order.id}`)
}

const handleCancel = async (order: OrderListItem) => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '选填',
    })
  } catch {
    return
  }
  try {
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {
    ElMessage.error('取消失败')
  }
}

const handleReceive = async (order: OrderListItem) => {
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
    await receiveOrder(order.id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (order: OrderListItem) => {
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
    await deleteOrder(order.id)
    orders.value = orders.value.filter(o => o.id !== order.id)
    ElMessage.success('订单已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="order-page">
    <div class="order-header">
      <el-button text class="back-btn" @click="router.push('/home')">
        <ArrowLeft />
        <span>返回首页</span>
      </el-button>
      <h2 class="order-title">我的订单</h2>
    </div>

    <!-- 状态 Tab -->
    <div class="order-tabs">
      <div
        v-for="tab in tabs"
        :key="String(tab.value)"
        class="order-tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="order-list">
      <template v-if="orders.length > 0">
        <div
          v-for="order in orders"
          :key="order.id"
          class="order-card"
        >
          <div class="order-head">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="statusClass(order.status)">
              {{ statusLabel(order.status) }}
            </span>
          </div>

          <div class="order-body" @click="handleViewDetail(order)">
            <div class="order-img">
              <span class="img-placeholder">暂无图片</span>
            </div>
            <div class="order-info">
              <p class="order-name">{{ order.firstItemName }}</p>
              <p class="order-meta">共 {{ order.itemCount }} 件商品</p>
            </div>
            <div class="order-price">
              <span class="order-total">¥{{ priceYuan(order.totalAmount) }}</span>
            </div>
          </div>

          <div class="order-footer">
            <span class="order-time">{{ order.createdAt }}</span>
            <div class="order-actions">
              <template v-if="order.status === OrderStatus.PENDING_PAYMENT">
                <el-button size="small" @click="handleCancel(order)">取消订单</el-button>
                <el-button size="small" type="danger" @click="handlePay(order)">立即付款</el-button>
              </template>
              <template v-else-if="order.status === OrderStatus.SHIPPED">
                <el-button size="small" type="primary" @click="handleReceive(order)">确认收货</el-button>
              </template>
              <template v-else-if="order.status === OrderStatus.COMPLETED || order.status === OrderStatus.CANCELLED">
                <el-button size="small" @click="handleViewDetail(order)">查看详情</el-button>
                <el-button size="small" type="danger" text @click="handleDelete(order)">删除</el-button>
              </template>
              <template v-else>
                <el-button size="small" @click="handleViewDetail(order)">查看详情</el-button>
              </template>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="order-empty">
        <div class="empty-icon">📋</div>
        <p>暂无订单</p>
        <el-button type="primary" size="small" @click="router.push('/home')">去逛逛</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="order-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<style scoped>
.order-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.order-header {
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

.order-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

/* ========== Tabs ========== */
.order-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.order-tab {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.order-tab:hover {
  color: #e2231a;
}

.order-tab.active {
  color: #e2231a;
  font-weight: 600;
  border-bottom-color: #e2231a;
  background: #fef5f5;
}

/* ========== 订单列表 ========== */
.order-list {
  min-height: 300px;
}

.order-card {
  background: #fff;
  margin-top: 12px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 13px;
  color: #999;
}

.order-status {
  font-size: 13px;
  font-weight: 600;
}

.s-pending { color: #e6a23c; }
.s-paid { color: #409eff; }
.s-shipped { color: #67c23a; }
.s-done { color: #999; }
.s-cancel { color: #c0c4cc; }

.order-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.order-body:hover {
  background: #fafafa;
}

.order-img {
  width: 72px;
  height: 72px;
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

.order-info {
  flex: 1;
  min-width: 0;
}

.order-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-meta {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.order-price {
  flex-shrink: 0;
}

.order-total {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-top: 1px solid #f5f5f5;
}

.order-time {
  font-size: 12px;
  color: #bbb;
}

.order-actions {
  display: flex;
  gap: 8px;
}

/* ========== 空状态 ========== */
.order-empty {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 0 0 8px 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 12px;
}

.order-empty p {
  font-size: 16px;
  color: #999;
  margin: 0 0 16px;
}

/* ========== 分页 ========== */
.order-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>