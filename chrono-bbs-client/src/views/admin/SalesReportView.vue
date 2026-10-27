<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminSalesReport } from '@/api/admin'

const loading = ref(false)
const report = ref<any>(null)

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminSalesReport()
    report.value = res.data
  } catch {
    report.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<template>
  <div class="sr-page" v-loading="loading">
    <h2>销售报表</h2>

    <template v-if="report">
      <div class="sr-cards">
        <div class="stat-card">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥{{ priceYuan(report.totalSales) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ report.totalOrders }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">注册用户</div>
          <div class="stat-value">{{ report.totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">在售商品</div>
          <div class="stat-value">{{ report.totalProducts }}</div>
        </div>
      </div>

      <div class="sr-section">
        <h3>近 7 日销售趋势</h3>
        <el-table :data="report.dailySales" stripe size="small">
          <el-table-column prop="date" label="日期" width="140" />
          <el-table-column label="销售额" align="right">
            <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="orderCount" label="订单数" align="center" width="100" />
        </el-table>
      </div>

      <div class="sr-section">
        <h3>热销 Top 5</h3>
        <el-table :data="report.topProducts" stripe size="small">
          <el-table-column prop="name" label="商品名称" min-width="200" />
          <el-table-column prop="sales" label="销量" width="100" align="center" />
          <el-table-column label="销售额" width="150" align="right">
            <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.sr-page {
  padding: 20px;
}

.sr-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 20px;
  color: #333;
}

.sr-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #e2231a;
}

.sr-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.sr-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #333;
}
</style>