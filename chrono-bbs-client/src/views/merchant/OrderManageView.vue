<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantOrders, shipOrder } from '@/api/merchant'
import type { OrderListItem } from '@/types/order'
import { OrderStatus } from '@/types/common'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const orders = ref<OrderListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref<number | ''>('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const statusLabel = (s: OrderStatus) => {
  const map: Record<number, string> = {
    [OrderStatus.PENDING_PAYMENT]: '待付款',
    [OrderStatus.PAID]: '待发货',
    [OrderStatus.SHIPPED]: '已发货',
    [OrderStatus.COMPLETED]: '已完成',
    [OrderStatus.CANCELLED]: '已取消',
  }
  return map[s] || '未知'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res = await getMerchantOrders(params)
    orders.value = res.data.list || []
    total.value = res.data.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleShip = async (order: OrderListItem) => {
  try {
    await shipOrder(order.id)
    ElMessage.success(`订单 ${order.orderNo} 已发货`)
    loadOrders()
  } catch {
    ElMessage.error('发货失败')
  }
}

const handleFilterChange = () => {
  page.value = 1
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="om-page">
    <div class="om-header">
      <h2>订单管理</h2>
    </div>

    <div class="om-filter">
      <el-select v-model="statusFilter" placeholder="订单状态" clearable @change="handleFilterChange" style="width: 150px">
        <el-option label="全部" value="" />
        <el-option label="待付款" :value="OrderStatus.PENDING_PAYMENT" />
        <el-option label="待发货" :value="OrderStatus.PAID" />
        <el-option label="已发货" :value="OrderStatus.SHIPPED" />
        <el-option label="已完成" :value="OrderStatus.COMPLETED" />
        <el-option label="已取消" :value="OrderStatus.CANCELLED" />
      </el-select>
    </div>

    <el-table :data="orders" v-loading="loading" stripe class="om-table">
      <el-table-column prop="orderNo" label="订单编号" width="200" />
      <el-table-column prop="firstItemName" label="商品" min-width="180" show-overflow-tooltip />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="itemCount" label="数量" width="70" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === OrderStatus.PAID ? 'warning' : row.status === OrderStatus.SHIPPED ? 'success' : 'info'"
            size="small"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === OrderStatus.PAID"
            size="small"
            type="primary"
            @click="handleShip(row)"
          >
            发货
          </el-button>
          <span v-else class="no-action">--</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="om-pagination" v-if="total > size">
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
.om-page {
  padding: 20px;
}

.om-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}

.om-filter {
  margin-bottom: 16px;
}

.om-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.no-action {
  color: #ccc;
  font-size: 13px;
}

.om-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>