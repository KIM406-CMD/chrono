<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, Delete } from '@element-plus/icons-vue'
import { getProductList, getCategoryTree } from '@/api/product'
import type { Product, Category } from '@/types/product'
import { formatPriceByPreference, generalSettings, reloadGeneralSettings } from '@/utils/settings'

const router = useRouter()
const route = useRoute()

const keyword = ref((route.query.keyword as string) || '')
const categoryId = ref(route.query.categoryId ? parseInt(route.query.categoryId as string) : 0)
const products = ref<Product[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 20
const sortBy = ref<'default' | 'price_asc' | 'price_desc' | 'sales_desc'>('default')

// 本地搜索数据（商品列表接口为公开接口，游客可用）
const allProducts = ref<Product[]>([])
const categoryTree = ref<Category[]>([])
const hotKeywords = ref<string[]>([])
const historyKeywords = ref<string[]>([])
const HISTORY_KEY = 'search_history'

// 搜索建议：基于已加载商品名称本地过滤
const querySuggest = (query: string, cb: (data: { value: string }[]) => void) => {
  const q = (query || '').trim().toLowerCase()
  if (!q) {
    cb([])
    return
  }
  const names = allProducts.value
    .map((item) => item.name)
    .filter((name) => name.toLowerCase().includes(q))
  cb(Array.from(new Set(names)).slice(0, 8).map((value) => ({ value })))
}

const loadHistory = () => {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    historyKeywords.value = raw ? JSON.parse(raw) : []
  } catch {
    historyKeywords.value = []
  }
}

const saveHistory = (kw: string) => {
  const next = [kw, ...historyKeywords.value.filter((item) => item !== kw)].slice(0, 10)
  historyKeywords.value = next
  localStorage.setItem(HISTORY_KEY, JSON.stringify(next))
}

const priceYuan = (fen: number) => formatPriceByPreference(fen)

const sorts = [
  { label: '综合', value: 'default' as const },
  { label: '价格 ↑', value: 'price_asc' as const },
  { label: '价格 ↓', value: 'price_desc' as const },
  { label: '销量', value: 'sales_desc' as const },
]

const hasQuery = () => !!keyword.value.trim() || !!categoryId.value

const collectCategoryIds = (node: Category): number[] => [
  node.id,
  ...(node.children || []).flatMap(collectCategoryIds),
]

const findCategory = (list: Category[], id: number): Category | null => {
  for (const node of list) {
    if (node.id === id) return node
    if (node.children?.length) {
      const found = findCategory(node.children, id)
      if (found) return found
    }
  }
  return null
}

const matchesCategory = (product: Product) => {
  if (!categoryId.value) return true
  const target = findCategory(categoryTree.value, categoryId.value)
  if (!target) return false
  return collectCategoryIds(target).includes(product.categoryId)
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
  } catch {
    categoryTree.value = []
  }
}

const loadAllProducts = async () => {
  loading.value = true
  try {
    const first = await getProductList({ page: 1, size: 100 })
    let list: Product[] = first.data.list || []
    const pages = Math.min(Math.ceil((first.data.total || 0) / 100), 10)
    for (let p = 2; p <= pages; p++) {
      const res = await getProductList({ page: p, size: 100 })
      list = list.concat(res.data.list || [])
    }
    allProducts.value = list
    hotKeywords.value = Array.from(
      new Set([...list].sort((a, b) => b.sales - a.sales).map((item) => item.name)),
    ).slice(0, 8)
  } catch {
    allProducts.value = []
    hotKeywords.value = []
  } finally {
    loading.value = false
  }
}

// 商品列表接口不支持 keyword，前端在已加载商品上做过滤、排序与分页
const loadProducts = () => {
  if (!hasQuery()) {
    products.value = []
    total.value = 0
    return
  }
  const q = keyword.value.trim().toLowerCase()
  let list = allProducts.value.filter((item) => {
    const textMatch = !q
      || item.name.toLowerCase().includes(q)
      || (item.brand || '').toLowerCase().includes(q)
    return textMatch && matchesCategory(item)
  })
  if (sortBy.value === 'price_asc') list = list.sort((a, b) => a.minPrice - b.minPrice)
  else if (sortBy.value === 'price_desc') list = list.sort((a, b) => b.minPrice - a.minPrice)
  else if (sortBy.value === 'sales_desc') list = list.sort((a, b) => b.sales - a.sales)
  total.value = list.length
  const start = (page.value - 1) * pageSize
  products.value = list.slice(start, start + pageSize)
}

const handleSearch = (kw?: string) => {
  const k = (kw ?? keyword.value).trim()
  if (!k) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  keyword.value = k
  categoryId.value = 0
  page.value = 1
  saveHistory(k)
  router.replace({ query: { keyword: k } })
  loadProducts()
}

const handleSort = (val: string) => {
  sortBy.value = val as typeof sortBy.value
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

const handleClearHistory = () => {
  historyKeywords.value = []
  localStorage.removeItem(HISTORY_KEY)
  ElMessage.success('已清除搜索历史')
}

onMounted(async () => {
  reloadGeneralSettings()
  loadHistory()
  await Promise.all([loadCategories(), loadAllProducts()])
  if (hasQuery()) {
    loadProducts()
  }
})

watch(
  () => route.query.keyword,
  (newKw) => {
    const k = newKw ? String(newKw) : ''
    if (k !== keyword.value) {
      keyword.value = k
      categoryId.value = 0
      page.value = 1
      loadProducts()
    }
  },
)

watch(
  () => route.query.categoryId,
  (newCat) => {
    const newId = newCat ? parseInt(String(newCat)) : 0
    if (newId !== categoryId.value) {
      categoryId.value = newId
      keyword.value = ''
      page.value = 1
      loadProducts()
    }
  },
)
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <el-button text class="back-btn" @click="router.push('/home')">
        <ArrowLeft />
      </el-button>
      <div class="search-bar">
        <el-autocomplete
          v-model="keyword"
          :fetch-suggestions="querySuggest"
          placeholder="搜索商品"
          size="large"
          :trigger-on-focus="false"
          @keyup.enter="handleSearch()"
          @select="(item: { value: string }) => handleSearch(item.value)"
        >
          <template #suffix>
            <el-button text class="search-icon-btn" @click="handleSearch()">
              <Search />
            </el-button>
          </template>
        </el-autocomplete>
      </div>
    </div>

    <!-- 有关键词 / 分类：搜索结果 -->
    <template v-if="hasQuery()">
      <div class="search-meta">
        <span class="result-count">共找到 <strong>{{ total }}</strong> 件相关商品</span>
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

      <div v-loading="loading" class="product-grid" :class="{ compact: generalSettings.compactList }">
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
            <el-image v-if="product.image" :src="product.image" fit="cover" />
            <span v-else class="img-placeholder">暂无图片</span>
          </div>
          <div class="card-info">
            <p class="card-name">{{ product.name }}</p>
            <div class="card-price-row">
              <span class="card-price">¥{{ priceYuan(product.minPrice) }}</span>
              <span class="card-sold" v-if="product.sales">已售 {{ product.sales }}</span>
            </div>
            <p class="card-brand" v-if="product.brand">{{ product.brand }}</p>
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

    <!-- 无关键词：热搜榜 + 搜索历史 -->
    <div v-else class="search-guide">
      <section v-if="hotKeywords.length" class="guide-section">
        <h3 class="guide-title">🔥 热门搜索</h3>
        <div class="tag-list">
          <span
            v-for="(kw, idx) in hotKeywords"
            :key="kw"
            class="guide-tag hot-tag"
            :class="{ 'top-three': idx < 3 }"
            @click="handleSearch(kw)"
          >
            {{ kw }}
          </span>
        </div>
      </section>

      <section v-if="historyKeywords.length" class="guide-section">
        <div class="guide-head">
          <h3 class="guide-title">🕘 搜索历史</h3>
          <el-button text size="small" :icon="Delete" class="clear-history" @click="handleClearHistory">
            清空
          </el-button>
        </div>
        <div class="tag-list">
          <span
            v-for="kw in historyKeywords"
            :key="kw"
            class="guide-tag history-tag"
            @click="handleSearch(kw)"
          >
            {{ kw }}
          </span>
        </div>
      </section>

      <div v-if="!hotKeywords.length && !historyKeywords.length" class="guide-section">
        <p class="empty-hint">输入关键词开始搜索商品</p>
      </div>
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
.search-bar :deep(.el-autocomplete) {
  width: 100%;
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

.product-grid.compact {
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}

.product-grid.compact .card-info {
  padding: 8px 10px 10px;
}

.product-grid.compact .card-name {
  font-size: 12px;
  -webkit-line-clamp: 1;
}

.product-grid.compact .card-price {
  font-size: 15px;
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

.card-img :deep(.el-image) {
  width: 100%;
  height: 100%;
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

.card-brand {
  margin: 4px 0 0;
  font-size: 11px;
  color: #aaa;
}

/* ========== 热搜 / 历史 ========== */
.search-guide {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.guide-section + .guide-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.guide-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.guide-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.clear-history {
  color: #999;
  font-size: 12px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.guide-tag {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.hot-tag {
  background: #f5f5f5;
  color: #666;
}

.hot-tag.top-three {
  color: #e2231a;
  background: #fff5f5;
  font-weight: 600;
}

.hot-tag:hover {
  color: #e2231a;
}

.history-tag {
  background: #fafafa;
  color: #888;
}

.history-tag:hover {
  color: #e2231a;
  background: #fff5f5;
}

/* ========== 空 ========== */
.empty-result {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 12px;
}

.empty-result p {
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
