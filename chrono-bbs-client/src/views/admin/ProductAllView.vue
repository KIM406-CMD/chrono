<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminProducts } from '@/api/admin'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getAdminProducts({ page: page.value, size })
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = (product: any) => {
  const action = product.status === 1 ? '下架' : '上架'
  ElMessage.success(`商品「${product.name}」已${action}`)
}

onMounted(() => { loadProducts() })
</script>

<template>
  <div class="pa-page">
    <h2>全平台商品管理</h2>

    <el-table :data="products" v-loading="loading" stripe class="pa-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="merchantName" label="所属店铺" width="120" />
      <el-table-column prop="brand" label="品牌" width="80" />
      <el-table-column label="价格" width="100" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.minPrice) }}</template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pa-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<style scoped>
.pa-page {
  padding: 20px;
}
.pa-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}
.pa-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.pa-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>