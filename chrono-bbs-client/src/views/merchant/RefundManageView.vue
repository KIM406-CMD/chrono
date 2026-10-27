<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRefundList, reviewRefund } from '@/api/refund'
import type { RefundItem } from '@/api/refund'

const loading = ref(false)
const refunds = ref<RefundItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadRefunds = async () => {
  loading.value = true
  try {
    const res = await getRefundList({ page: page.value, size })
    refunds.value = res.data.list || []
    total.value = res.data.total
  } catch {
    refunds.value = []
  } finally {
    loading.value = false
  }
}

const handleApprove = async (item: RefundItem) => {
  try {
    await ElMessageBox.confirm('确定要同意该退款申请吗？', '审核退款', {
      confirmButtonText: '同意退款',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await reviewRefund(item.id, { approved: true })
    ElMessage.success('已同意退款')
    loadRefunds()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (item: RefundItem) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核退款', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
    })
    await reviewRefund(item.id, { approved: false, reason: value })
    ElMessage.success('已拒绝退款')
    loadRefunds()
  } catch {
    // cancel
  }
}

const typeMap: Record<number, string> = { 1: '退款', 2: '退货' }
const statusMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }

onMounted(() => {
  loadRefunds()
})
</script>

<template>
  <div class="rm-page">
    <h2>退款审核</h2>

    <el-table :data="refunds" v-loading="loading" stripe class="rm-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }">{{ typeMap[row.type] }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
      <el-table-column label="金额" width="100" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'"
            size="small"
          >
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row)">同意</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
          <span v-else class="no-action">--</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="rm-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadRefunds"
      />
    </div>
  </div>
</template>

<style scoped>
.rm-page {
  padding: 20px;
}

.rm-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}

.rm-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.no-action {
  color: #ccc;
  font-size: 13px;
}

.rm-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>