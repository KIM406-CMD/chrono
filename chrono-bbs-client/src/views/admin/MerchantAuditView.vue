<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchants, reviewMerchant } from '@/api/admin'

interface MerchantRow {
  id: number
  applicantUsername: string
  shopName: string
  contactPhone: string
  status: number
  reviewReason: string | null
  reviewedAt: string | null
  createdAt: string
}

const loading = ref(false)
const merchants = ref<MerchantRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const activeStatus = ref<number | 'all'>('all')

const tabs = [
  { label: '全部', value: 'all' as const },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 },
]

const statusMap: Record<number, { text: string; type: 'warning' | 'success' | 'danger' }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
}

const loadData = async () => {
  loading.value = true
  try {
    const params: { status?: number; page: number; size: number } = { page: page.value, size }
    if (activeStatus.value !== 'all') {
      params.status = activeStatus.value
    }
    const res = await getMerchants(params)
    merchants.value = res.data.list || []
    total.value = res.data.total
  } catch {
    merchants.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = (status: number | 'all') => {
  activeStatus.value = status
  page.value = 1
  loadData()
}

const handleApprove = async (item: MerchantRow) => {
  try {
    await ElMessageBox.confirm(`确定审核通过商家「${item.shopName}」吗？`, '审核商家', {
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch {
    return
  }
  try {
    await reviewMerchant(item.id, { approved: 1 })
    ElMessage.success(`商家「${item.shopName}」审核通过`)
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (item: MerchantRow) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核商家', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
    })
    await reviewMerchant(item.id, { approved: 0, reviewReason: value })
    ElMessage.success(`已拒绝商家「${item.shopName}」`)
    loadData()
  } catch {
    // cancel
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ma-page">
    <h2>商家审核</h2>

    <div class="ma-tabs">
      <div
        v-for="tab in tabs"
        :key="String(tab.value)"
        class="ma-tab"
        :class="{ active: activeStatus === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <el-table :data="merchants" v-loading="loading" stripe class="ma-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="applicantUsername" label="用户名" width="120" />
      <el-table-column prop="shopName" label="店铺名称" min-width="150" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核意见" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reviewReason || '—' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
          <span v-else class="no-action">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="merchants.length === 0 && !loading" class="ma-empty">
      <p>暂无商家数据</p>
    </div>

    <div v-if="total > size" class="ma-pagination">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<style scoped>
.ma-page {
  padding: 20px;
}
.ma-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}

.ma-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.ma-tab {
  padding: 8px 16px;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.ma-tab:hover {
  color: #e2231a;
}

.ma-tab.active {
  background: #e2231a;
  color: #fff;
}

.ma-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.ma-empty {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  color: #999;
}

.ma-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.no-action {
  color: #ccc;
  font-size: 13px;
}
</style>
