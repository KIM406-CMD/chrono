<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { getProductList, getCategoryTree } from '@/api/product'
import type { Product, Category } from '@/types/product'

const router = useRouter()
const route = useRoute()

const keyword = ref((route.query.keyword as string) || (route.query.q as string) || '')
const categoryId = ref(route.query.categoryId ? parseInt(route.query.categoryId as string) : 0)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 20
const sortBy = ref<'default' | 'price_asc' | 'price_desc' | 'sales'>('default')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const currentCategoryName = ref('')

const findCategoryName = (cats: Category[], id: number): string => {
  for (const c of cats) {
    if (c.id === id) return c.name
    if (c.children) {
      const found = findCategoryName(c.children, id)
      if (found) return found
    }
  }
  return ''
}

const sorts = [
  { label: '综合', value: 'default' as const },
  { label: '价格 ↑', value: 'price_asc' as const },
  { label: '价格 ↓', value: 'price_desc' as const },
  { label: '销量', value: 'sales' as const },
]

const hasQuery = () => !!keyword.value || !!categoryId.value

const loadProducts = async () => {
  if (!hasQuery()) {
    products.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: pageSize,
    }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    if (sortBy.value === 'price_asc') {
      params.sortField = 'price'
      params.sortOrder = 'asc'
    } else if (sortBy.value === 'price_desc') {
      params.sortField = 'price'
      params.sortOrder = 'desc'
    } else if (sortBy.value === 'sales') {
      params.sortField = 'sales'
      params.sortOrder = 'desc'
    }
    const res = await getProductList(params)
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  categoryId.value = 0
  page.value = 1
  router.replace({ query: { keyword: keyword.value } })
  loadProducts()
}

const handleSort = (val: string) => {
  sortBy.value = val as any
  page.value = 1
  loadProducts()
}

const handleProductClick = (product: Product) => {
  router.push(`/product/${product.id}`)
}

const handlePageChange = (p: number) => {
  page.value = p
  loadProducts()
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
    if (categoryId.value) {
      currentCategoryName.value = findCategoryName(categories.value, categoryId.value)
    }
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadCategories()
  if (hasQuery()) {
    loadProducts()
  }
})

watch(() => route.query.keyword, (newKw) => {
  if (newKw !== undefined && newKw !== keyword.value) {
    keyword.value = (newKw as string) || ''
    categoryId.value = 0
    page.value = 1
    loadProducts()
  }
})

watch(() => route.query.categoryId, (newCat) => {
  const newId = newCat ? parseInt(newCat as string) : 0
  if (newId !== categoryId.value) {
    categoryId.value = newId
    keyword.value = ''
    page.value = 1
    currentCategoryName.value = newId ? findCategoryName(categories.value, newId) : ''
    loadProducts()
  }
})
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <el-button text class="back-btn" @click="router.push('/home')">
        <ArrowLeft />
      </el-button>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #suffix>
            <el-button
              text
              class="search-icon-btn"
              @click="handleSearch"
            >
              <Search />
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <template v-if="hasQuery()">
      <div class="search-meta">
        <span class="result-count">
          <template v-if="categoryId">
            分类：<strong>{{ currentCategoryName }}</strong> ·
          </template>
          共找到 <strong>{{ total }}</strong> 件相关商品
        </span>
        <div class="sort-bar">
          <span
            v-for="s in sorts"
            :key="s.value"
            class="sort-item"
            :class="{ active: sortBy === s.value }"
            @click="handleSort(s.value)"
          >
            {{ s.label }}
          </span>
        </div>
      </div>

      <div v-loading="loading" class="product-grid">
        <div v-if="products.length === 0 && !loading" class="empty-result">
          <p class="empty-icon">🔍</p>
          <p>没有找到相关商品</p>
          <p class="empty-hint">尝试更换搜索词</p>
        </div>

        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="handleProductClick(product)"
        >
          <div class="card-img">
            <span class="img-placeholder">暂无图片</span>
          </div>
          <div class="card-info">
            <p class="card-name">{{ product.name }}</p>
            <div class="card-price-row">
              <span class="card-price">¥{{ priceYuan(product.minPrice) }}</span>
              <span class="card-sold" v-if="product.totalSales">
                已售 {{ product.totalSales }}
              </span>
            </div>
            <p class="card-merchant" v-if="product.merchantName">
              {{ product.merchantName }}
            </p>
          </div>
        </div>
      </div>

      <div class="search-pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </template>

    <div v-else class="search-empty">
      <p class="empty-icon">🔍</p>
      <p>输入关键词搜索商品</p>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 18px;
  color: #666;
  padding: 0;
}
.back-btn:hover { color: #e2231a; }

.search-bar {
  flex: 1;
}

.search-icon-btn {
  font-size: 20px;
  color: #999;
}
.search-icon-btn:hover { color: #e2231a; }

/* ========== meta ========== */
.search-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 6px;
}

.result-count {
  font-size: 13px;
  color: #666;
}
.result-count strong { color: #e2231a; }

.sort-bar {
  display: flex;
  gap: 4px;
}

.sort-item {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.sort-item:hover { color: #e2231a; }
.sort-item.active {
  color: #fff;
  background: #e2231a;
  font-weight: 600;
}

/* ========== 商品网格 ========== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.product-card {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.card-img {
  width: 100%;
  aspect-ratio: 1;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
}

.img-placeholder { font-size: 12px; color: #bbb; }

.card-info {
  padding: 10px 12px 14px;
}

.card-name {
  font-size: 13px;
  color: #333;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.card-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.card-price {
  font-size: 18px;
  font-weight: 700;
  color: #e2231a;
}

.card-sold {
  font-size: 11px;
  color: #bbb;
}

.card-merchant {
  margin: 4px 0 0;
  font-size: 11px;
  color: #aaa;
}

/* ========== 空 ========== */
.search-empty, .empty-result {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 12px;
}

.empty-result p, .search-empty p {
  font-size: 16px;
  color: #999;
  margin: 4px 0;
}

.empty-hint {
  font-size: 13px;
  color: #bbb;
}

.search-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>