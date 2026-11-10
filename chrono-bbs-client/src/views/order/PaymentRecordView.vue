<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPaymentRecords, type PaymentRecord } from '@/api/payment'

const loading = ref(false)
const records = ref<PaymentRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const priceYuan = (fen: number) => ((fen || 0) / 100).toFixed(2)
const methodMap: Record<number, string> = { 1: '银行卡', 2: '二维码' }
const statusMap: Record<number, string> = { 0: '处理中', 1: '成功', 2: '失败' }

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getPaymentRecords({ page: page.value, size })
    records.value = res.data.list || []
    total.value = res.data.total || 0
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(loadRecords)
</script>

<template>
  <div class="pr-page">
    <div class="pr-header">
      <h2>支付记录</h2>
      <span class="pr-tip">记录每笔支付的支付方式、金额与时间</span>
    </div>

    <el-table :data="records" v-loading="loading" stripe class="pr-table">
      <el-table-column prop="orderNo" label="订单号" min-width="190" />
      <el-table-column label="支付方式" width="100" align="center">
        <template #default="{ row }">{{ methodMap[row.payMethod] || '—' }}</template>
      </el-table-column>
      <el-table-column label="卡号" width="190">
        <template #default="{ row }">{{ row.cardNo || '—' }}</template>
      </el-table-column>
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'"
            size="small"
          >
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" width="180">
        <template #default="{ row }">{{ row.paidAt || '—' }}</template>
      </el-table-column>
    </el-table>

    <div class="pr-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadRecords"
      />
    </div>
  </div>
</template>

<style scoped>
.pr-page {
  padding: 20px;
}

.pr-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
}

.pr-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.pr-tip {
  font-size: 12px;
  color: #999;
}

.pr-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.pr-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
