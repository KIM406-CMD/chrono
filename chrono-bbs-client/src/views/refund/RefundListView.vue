<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getRefundList, getRefundDetail, submitRefundReturn } from '@/api/refund'
import type { RefundItem } from '@/api/refund'

const router = useRouter()

const loading = ref(false)
const refunds = ref<RefundItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<RefundItem | null>(null)

const typeMap: Record<number, string> = { 1: '仅退款', 2: '退货退款' }
const statusMap: Record<number, { text: string; type: 'warning' | 'success' | 'danger' | 'info' }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
  3: { text: '待寄回', type: 'warning' },
  4: { text: '待商家收货', type: 'info' },
  5: { text: '退货完成', type: 'success' },
}

const returnVisible = ref(false)
const returnSubmitting = ref(false)
const returnTarget = ref<RefundItem | null>(null)
const returnForm = reactive({ returnCompany: '', returnLogisticsNo: '' })

const openReturn = (row: RefundItem) => {
  returnTarget.value = row
  returnForm.returnCompany = ''
  returnForm.returnLogisticsNo = ''
  returnVisible.value = true
}

const handleSubmitReturn = async () => {
  if (!returnTarget.value) return
  if (!returnForm.returnCompany.trim() || !returnForm.returnLogisticsNo.trim()) {
    ElMessage.warning('请填写快递公司与物流单号')
    return
  }
  returnSubmitting.value = true
  try {
    await submitRefundReturn(returnTarget.value.id, {
      returnCompany: returnForm.returnCompany.trim(),
      returnLogisticsNo: returnForm.returnLogisticsNo.trim(),
    })
    ElMessage.success('退货物流已提交')
    returnVisible.value = false
    loadRefunds()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    returnSubmitting.value = false
  }
}

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

const openDetail = async (row: RefundItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getRefundDetail(row.id)
    detail.value = res.data
  } catch {
    detail.value = row
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadRefunds()
})
</script>

<template>
  <div class="rf-page">
    <div class="rf-header">
      <el-button text class="back-btn" @click="router.push('/orders')">
        <ArrowLeft />
        <span>返回订单</span>
      </el-button>
      <h2 class="rf-title">退款/售后</h2>
    </div>

    <el-table :data="refunds" v-loading="loading" stripe class="rf-table">
      <el-table-column prop="id" label="退款单" width="90" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column prop="productName" label="商品" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">{{ typeMap[row.type] || '—' }}</template>
      </el-table-column>
      <el-table-column label="退款金额" width="110" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.type === 2 && row.status === 3"
            size="small"
            type="warning"
            @click="openReturn(row)"
          >填写退货物流</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="rf-pagination">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadRefunds"
      />
    </div>

    <el-dialog v-model="detailVisible" title="退款详情" width="560px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="detail" :column="1" border>
          <el-descriptions-item label="退款单号">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="订单 ID">{{ detail.orderId }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ detail.productName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeMap[detail.type] || '—' }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ priceYuan(detail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[detail.status]?.type || 'info'" size="small">
              {{ statusMap[detail.status]?.text || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因">{{ detail.reason }}</el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ detail.reviewReason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.reviewedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.returnCompany || detail.returnLogisticsNo" label="退货物流">
            {{ detail.returnCompany || '—' }} {{ detail.returnLogisticsNo || '' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.returnedAt" label="寄回时间">{{ detail.returnedAt }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.returnReceivedAt" label="商家收货时间">{{ detail.returnReceivedAt }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnVisible" title="填写退货物流" width="420px">
      <el-form label-width="90px">
        <el-form-item label="快递公司">
          <el-input v-model="returnForm.returnCompany" maxlength="50" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="returnForm.returnLogisticsNo" maxlength="50" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" :loading="returnSubmitting" @click="handleSubmitReturn">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.rf-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.rf-header {
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

.rf-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.rf-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.rf-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
