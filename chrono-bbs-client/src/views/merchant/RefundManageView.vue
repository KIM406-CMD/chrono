<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmRefundReturnReceived, reviewRefund } from '@/api/refund'
import { getMerchantRefunds } from '@/api/merchant'
import { getAdminRefunds } from '@/api/admin'
import { REFUND_AUDIT_ENABLED } from '@/config/features'
import type { RefundItem } from '@/api/refund'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))

const loading = ref(false)
const refunds = ref<RefundItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref<number | ''>('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadRefunds = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; status?: number } = { page: page.value, size }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = isAdmin.value
      ? await getAdminRefunds(params)
      : await getMerchantRefunds(params)
    refunds.value = res.data.list || []
    total.value = res.data.total
  } catch {
    refunds.value = []
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  loadRefunds()
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
    await reviewRefund(item.id, { approved: 1 })
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
    await reviewRefund(item.id, { approved: 0, reviewReason: value })
    ElMessage.success('已拒绝退款')
    loadRefunds()
  } catch {
    // cancel
  }
}

const typeMap: Record<number, string> = { 1: '退款', 2: '退货' }
const statusMap: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  3: '待寄回',
  4: '待商家收货',
  5: '退货完成',
}

const handleConfirmReturn = async (item: RefundItem) => {
  try {
    await ElMessageBox.confirm('确认已收到买家退回的商品？确认后将完成退款。', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await confirmRefundReturnReceived(item.id)
    ElMessage.success('已确认收货，退款完成')
    loadRefunds()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  if (REFUND_AUDIT_ENABLED) loadRefunds()
})
</script>

<template>
  <div v-if="REFUND_AUDIT_ENABLED" class="rm-page">
    <div class="rm-header">
      <h2>退款审核</h2>
      <el-select
        v-model="statusFilter"
        placeholder="全部状态"
        clearable
        style="width: 140px"
        @change="handleFilterChange"
      >
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="待寄回" :value="3" />
        <el-option label="待商家收货" :value="4" />
        <el-option label="退货完成" :value="5" />
      </el-select>
    </div>

    <el-table :data="refunds" v-loading="loading" stripe class="rm-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }">{{ typeMap[row.type] }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
      <el-table-column label="退货物流" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.returnLogisticsNo ? `${row.returnCompany || ''} ${row.returnLogisticsNo}` : '—' }}
        </template>
      </el-table-column>
      <el-table-column label="金额" width="100" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag
            :type="
              row.status === 1 || row.status === 5
                ? 'success'
                : row.status === 2
                  ? 'danger'
                  : row.status === 4
                    ? 'info'
                    : 'warning'
            "
            size="small"
          >
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row)">同意</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
          <el-button
            v-else-if="row.type === 2 && row.status === 4"
            size="small"
            type="primary"
            @click="handleConfirmReturn(row)"
          >确认收货</el-button>
          <span v-else class="no-action">{{ row.type === 2 && row.status === 3 ? '待买家寄回' : '--' }}</span>
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

  <div v-else class="rm-unavailable">
    <el-empty description="商家退款审核接口待接入，功能暂不可用" />
  </div>
</template>

<style scoped>
.rm-unavailable {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  background: #fff;
  border-radius: 8px;
}

.rm-page {
  padding: 20px;
}

.rm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.rm-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
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