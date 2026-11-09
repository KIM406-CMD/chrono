<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminProducts, setAdminProductStatus, deleteAdminProduct } from '@/api/admin'

const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const statusFilter = ref<number | ''>('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadProducts = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; keyword?: string; status?: number } = {
      page: page.value,
      size,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminProducts(params)
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadProducts()
}

const handleReset = () => {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  loadProducts()
}

const handleToggleStatus = async (row: any) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}商品「${row.name}」吗？`, `${action}商品`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await setAdminProductStatus(row.id, nextStatus)
    ElMessage.success(`已${action}商品`)
    loadProducts()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品「${row.name}」吗？删除后不可恢复。`, '删除商品', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteAdminProduct(row.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { loadProducts() })
</script>

<template>
  <div class="pa-page">
    <h2>全平台商品管理</h2>

    <div class="pa-filter">
      <el-input
        v-model="keyword"
        placeholder="商品名称 / 品牌"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="products" v-loading="loading" stripe class="pa-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="所属店铺" width="120">
        <template #default="{ row }">{{ row.merchantName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="80" />
      <el-table-column label="价格" width="100" align="right">
        <template #default="{ row }">{{ row.minPrice != null ? `¥${priceYuan(row.minPrice)}` : '—' }}</template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
.pa-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
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