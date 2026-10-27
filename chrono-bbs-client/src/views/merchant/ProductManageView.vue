<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'
import type { Product } from '@/types/product'

const loading = ref(false)
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProductList({ page: page.value, size, keyword: keyword.value || undefined })
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleEdit = (product: Product) => {
  ElMessage.info(`编辑商品 ${product.name}（功能待接入后端）`)
}

const handleDelete = async (product: Product) => {
  try {
    await ElMessageBox.confirm(`确定要下架商品「${product.name}」吗？`, '下架商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  ElMessage.success(`商品「${product.name}」已下架`)
}

const handleAdd = () => {
  ElMessage.info('新增商品（功能待接入后端）')
}

const handleSearch = () => {
  page.value = 1
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<template>
  <div class="pm-page">
    <div class="pm-header">
      <h2>商品管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
    </div>

    <div class="pm-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <el-table :data="products" v-loading="loading" stripe class="pm-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-cell">
            <div class="product-img"><span>暂无图片</span></div>
            <div>
              <p class="p-name">{{ row.name }}</p>
              <p class="p-brand">{{ row.brand }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.minPrice) }}</template>
      </el-table-column>
      <el-table-column prop="totalSales" label="销量" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button text size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(row)">
            下架
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pm-pagination" v-if="total > size">
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
.pm-page {
  padding: 20px;
}

.pm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pm-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.pm-toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.pm-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  border: 1px solid #eee;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #bbb;
  flex-shrink: 0;
}

.p-name {
  margin: 0;
  font-size: 14px;
  color: #333;
}
.p-brand {
  margin: 2px 0 0;
  font-size: 12px;
  color: #999;
}

.pm-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>