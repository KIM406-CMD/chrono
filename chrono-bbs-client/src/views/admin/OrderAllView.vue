<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminOrders } from '@/api/admin'
import { OrderStatus } from '@/types/common'

const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const orderNo = ref('')
const statusFilter = ref<number | ''>('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const statusLabel = (s: number) => {
  const map: Record<number, string> = {
    [OrderStatus.PENDING_PAYMENT]: '待付款',
    [OrderStatus.PAID]: '待发货',
    [OrderStatus.SHIPPED]: '已发货',
    [OrderStatus.COMPLETED]: '已完成',
    [OrderStatus.CANCELLED]: '已取消',
    [OrderStatus.REFUNDING]: '退款中',
    [OrderStatus.REFUNDED]: '已退款',
  }
  return map[s] || '未知'
}

const statusTagType = (s: number) => {
  if (s === OrderStatus.PAID || s === OrderStatus.REFUNDING) return 'warning'
  if (s === OrderStatus.COMPLETED) return 'success'
  if (s === OrderStatus.REFUNDED) return 'danger'
  return 'info'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; orderNo?: string; status?: number } = {
      page: page.value,
      size,
    }
    if (orderNo.value.trim()) params.orderNo = orderNo.value.trim()
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminOrders(params)
    orders.value = res.data.list || []
    total.value = res.data.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadOrders()
}

const handleReset = () => {
  orderNo.value = ''
  statusFilter.value = ''
  page.value = 1
  loadOrders()
}

onMounted(() => { loadOrders() })
</script>

<template>
  <div class="oa-page">
    <h2>全平台订单管理</h2>

    <div class="oa-filter">
      <el-input
        v-model="orderNo"
        placeholder="订单编号"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 140px">
        <el-option label="待付款" :value="OrderStatus.PENDING_PAYMENT" />
        <el-option label="待发货" :value="OrderStatus.PAID" />
        <el-option label="已发货" :value="OrderStatus.SHIPPED" />
        <el-option label="已完成" :value="OrderStatus.COMPLETED" />
        <el-option label="已取消" :value="OrderStatus.CANCELLED" />
        <el-option label="退款中" :value="OrderStatus.REFUNDING" />
        <el-option label="已退款" :value="OrderStatus.REFUNDED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" stripe class="oa-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="200" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag
            :type="statusTagType(row.status)"
            size="small"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
    </el-table>

    <div class="oa-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<style scoped>
.oa-page {
  padding: 20px;
}
.oa-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}
.oa-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.oa-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.oa-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>