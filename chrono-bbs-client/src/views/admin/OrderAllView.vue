<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminOrders } from '@/api/admin'
import { OrderStatus } from '@/types/common'

const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const statusLabel = (s: number) => {
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
    const res = await getAdminOrders({ page: page.value, size })
    orders.value = res.data.list || []
    total.value = res.data.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadOrders() })
</script>

<template>
  <div class="oa-page">
    <h2>全平台订单管理</h2>

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
            :type="row.status === OrderStatus.PAID ? 'warning' : row.status === OrderStatus.COMPLETED ? 'success' : 'info'"
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