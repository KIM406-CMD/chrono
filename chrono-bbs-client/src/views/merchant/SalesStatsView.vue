<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { getMerchantStats } from '@/api/merchant'
import BaseChart from '@/components/chart/BaseChart.vue'

const loading = ref(false)
const report = ref<any>(null)

const priceYuan = (fen: number) => (fen / 100).toFixed(2)
const yuanValue = (fen: number) => Number(((fen || 0) / 100).toFixed(2))

const trendOption = computed(() => {
  const rows = report.value?.salesTrend || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 24, top: 20, bottom: 32 },
    xAxis: { type: 'category', boundaryGap: false, data: rows.map((row: any) => row.date) },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      smooth: true,
      symbolSize: 6,
      data: rows.map((row: any) => yuanValue(row.amount)),
      itemStyle: { color: '#e2231a' },
      lineStyle: { color: '#e2231a' },
      areaStyle: { opacity: 0.12 },
    }],
  }
})

const topOption = computed(() => {
  const rows = [...(report.value?.topProducts || [])].slice(0, 5).reverse()
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 170, right: 30, top: 16, bottom: 28 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: rows.map((row: any) => row.productName) },
    series: [{
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: { color: '#e2231a' },
      data: rows.map((row: any) => yuanValue(row.totalAmount)),
    }],
  }
})

const categoryOption = computed(() => {
  const rows = report.value?.salesByCategory || []
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['42%', '66%'],
      center: ['50%', '44%'],
      label: { formatter: '{b}\n{d}%' },
      data: rows.map((row: any) => ({ name: row.categoryName, value: yuanValue(row.totalAmount) })),
    }],
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMerchantStats()
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
  <div class="ss-page" v-loading="loading">
    <h2>销售统计</h2>

    <template v-if="report">
      <div class="ss-cards">
        <div class="stat-card">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥{{ priceYuan(report.totalSales) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">订单数</div>
          <div class="stat-value">{{ report.orderCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">客单价</div>
          <div class="stat-value">¥{{ priceYuan(report.orderCount ? report.totalSales / report.orderCount : 0) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">销售分类</div>
          <div class="stat-value">{{ report.salesByCategory?.length || 0 }}</div>
        </div>
      </div>

      <div class="ss-section">
        <h3>销售趋势</h3>
        <BaseChart :option="trendOption" height="280px" />
        <el-table :data="report.salesTrend" stripe size="small">
          <el-table-column prop="date" label="日期" width="140" />
          <el-table-column label="销售额" align="right">
            <template #default="{ row }">¥{{ priceYuan(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="ss-section">
        <h3>热销商品 Top 5</h3>
        <BaseChart :option="topOption" height="240px" />
        <el-table :data="(report.topProducts || []).slice(0, 5)" stripe size="small">
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="salesCount" label="销量" width="100" align="center" />
          <el-table-column label="销售额" width="150" align="right">
            <template #default="{ row }">¥{{ priceYuan(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="ss-section">
        <h3>分类销售额</h3>
        <BaseChart :option="categoryOption" height="300px" />
        <el-table :data="report.salesByCategory" stripe size="small">
          <el-table-column prop="categoryName" label="分类" min-width="200" />
          <el-table-column label="销售额" width="150" align="right">
            <template #default="{ row }">¥{{ priceYuan(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.ss-page {
  padding: 20px;
}

.ss-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 20px;
  color: #333;
}

.ss-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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

.ss-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.ss-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #333;
}
</style>