<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getProductList, getCategoryTree } from '@/api/product'
import {
  searchProducts,
  getSearchHistory,
  clearSearchHistory,
  getHotSearch,
  type SearchItem,
} from '@/api/search'
import { useUserStore } from '@/stores/user'
import type { Product, Category } from '@/types/product'
import { formatPriceByPreference, generalSettings, reloadGeneralSettings } from '@/utils/settings'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref((route.query.keyword as string) || '')
const categoryId = ref(route.query.categoryId ? parseInt(route.query.categoryId as string) : 0)
const products = ref<Product[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 20
const sortBy = ref<'default' | 'price_asc' | 'price_desc' | 'sales_desc' | 'time_desc'>('default')
const brandFilter = ref('')
const minPriceYuan = ref<number | undefined>(undefined)
const maxPriceYuan = ref<number | undefined>(undefined)

// 本地搜索数据（商品列表接口为公开接口，游客可用）
const allProducts = ref<Product[]>([])
const categoryTree = ref<Category[]>([])
const hotKeywords = ref<string[]>([])
const historyKeywords = ref<string[]>([])
const HISTORY_KEY = 'search_history'

const loadHistory = async () => {
  if (userStore.isLoggedIn) {
    try {
      const res = await getSearchHistory()
      historyKeywords.value = (res.data || []).slice(0, 20)
      return
    } catch {
      // 回退到本地历史
    }
  }
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    historyKeywords.value = raw ? JSON.parse(raw) : []
  } catch {
    historyKeywords.value = []
  }
}

const loadHotKeywords = async () => {
  try {
    const res = await getHotSearch()
    if (res.data?.length) {
      hotKeywords.value = res.data
      return
    }
  } catch {
    // 回退到本地热词
  }
  hotKeywords.value = Array.from(
    new Set([...allProducts.value].sort((a, b) => b.sales - a.sales).map((item) => item.name)),
  ).slice(0, 8)
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
  { label: '上架时间', value: 'time_desc' as const },
]

const hasQuery = () =>
  !!keyword.value.trim()
  || !!categoryId.value
  || !!brandFilter.value
  || minPriceYuan.value != null
  || maxPriceYuan.value != null

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

// 分类路径（用于展示“一级 / 二级 / 三级”）
const findCategoryPath = (list: Category[], id: number, path: string[] = []): string[] => {
  for (const node of list) {
    const next = [...path, node.name]
    if (node.id === id) return next
    if (node.children?.length) {
      const found = findCategoryPath(node.children, id, next)
      if (found.length) return found
    }
  }
  return []
}

const categoryPath = computed(() =>
  categoryId.value ? findCategoryPath(categoryTree.value, categoryId.value) : [],
)

// 品牌选项来自已加载商品与当前搜索结果
const brandOptions = computed(() =>
  Array.from(
    new Set(
      [...allProducts.value, ...products.value]
        .map((item) => item.brand)
        .filter(Boolean),
    ),
  ).sort(),
)

const applyFilters = () => {
  page.value = 1
  loadProducts()
}

const resetFilters = () => {
  brandFilter.value = ''
  minPriceYuan.value = undefined
  maxPriceYuan.value = undefined
  page.value = 1
  loadProducts()
}

// 级联分类切换
const handleCategoryChange = (val: number | null) => {
  const id = val ?? 0
  categoryId.value = id
  page.value = 1
  if (id) keyword.value = ''
  const query: Record<string, string> = {}
  if (id) query.categoryId = String(id)
  else if (keyword.value.trim()) query.keyword = keyword.value.trim()
  router.replace({ query })
  loadProducts()
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
  } catch {
    allProducts.value = []
  } finally {
    loading.value = false
  }
}

// 搜索结果项转换为商品卡片所需的 Product 结构
const toProduct = (item: SearchItem): Product => ({
  id: item.id,
  name: item.name,
  description: item.description || '',
  image: item.image,
  images: [],
  brand: item.brand || '',
  categoryId: item.categoryId || 0,
  categoryName: item.categoryName || '',
  merchantId: 0,
  status: 1,
  sales: item.sales,
  minPrice: item.minPrice,
  createdAt: '',
  updatedAt: '',
})

// 无关键词（仅分类/筛选）时，基于已加载商品本地过滤
const applyLocalFilter = () => {
  const q = keyword.value.trim().toLowerCase()
  let list = allProducts.value.filter((item) => {
    const textMatch = !q
      || item.name.toLowerCase().includes(q)
      || (item.brand || '').toLowerCase().includes(q)
    if (!textMatch || !matchesCategory(item)) return false
    if (brandFilter.value && item.brand !== brandFilter.value) return false
    if (minPriceYuan.value != null && item.minPrice < minPriceYuan.value * 100) return false
    if (maxPriceYuan.value != null && item.minPrice > maxPriceYuan.value * 100) return false
    return true
  })
  if (sortBy.value === 'price_asc') list = list.sort((a, b) => a.minPrice - b.minPrice)
  else if (sortBy.value === 'price_desc') list = list.sort((a, b) => b.minPrice - a.minPrice)
  else if (sortBy.value === 'sales_desc') list = list.sort((a, b) => b.sales - a.sales)
  else if (sortBy.value === 'time_desc') {
    list = list.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || '') || b.id - a.id)
  }
  total.value = list.length
  const start = (page.value - 1) * pageSize
  products.value = list.slice(start, start + pageSize)
}

const loadProducts = async () => {
  if (!hasQuery()) {
    products.value = []
    total.value = 0
    return
  }

  // 有关键词时走后端搜索接口；接口异常时回退本地过滤
  const q = keyword.value.trim()
  if (q) {
    loading.value = true
    try {
      const res = await searchProducts({
        keyword: q,
        categoryId: categoryId.value || undefined,
        brand: brandFilter.value || undefined,
        minPrice: minPriceYuan.value != null ? Math.round(minPriceYuan.value * 100) : undefined,
        maxPrice: maxPriceYuan.value != null ? Math.round(maxPriceYuan.value * 100) : undefined,
        sort: sortBy.value === 'default' ? undefined : sortBy.value,
        page: page.value,
        size: pageSize,
      })
      products.value = (res.data.list || []).map(toProduct)
      total.value = res.data.total || 0
      return
    } catch {
      // 回退到本地过滤
    } finally {
      loading.value = false
    }
  }

  applyLocalFilter()
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

const handleClearHistory = async () => {
  if (userStore.isLoggedIn) {
    try {
      await clearSearchHistory()
    } catch {
      // 接口异常时仅清理本地
    }
  }
  historyKeywords.value = []
  localStorage.removeItem(HISTORY_KEY)
  ElMessage.success('已清除搜索历史')
}

onMounted(async () => {
  reloadGeneralSettings()
  await Promise.all([loadCategories(), loadAllProducts()])
  await Promise.all([loadHistory(), loadHotKeywords()])
  if (hasQuery()) {
    await loadProducts()
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
      void loadProducts()
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
      void loadProducts()
    }
  },
)
</script>

<template>
  <div class="search-page">
    <!-- 有关键词 / 分类 / 筛选条件：搜索结果 -->
    <template v-if="hasQuery()">
      <div class="filter-bar">
        <el-cascader
          v-model="categoryId"
          :options="categoryTree"
          :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
          clearable
          placeholder="全部分类"
          class="filter-category"
          @change="handleCategoryChange"
        />
        <el-select
          v-model="brandFilter"
          clearable
          placeholder="全部品牌"
          class="filter-brand"
          @change="applyFilters"
        >
          <el-option v-for="brand in brandOptions" :key="brand" :label="brand" :value="brand" />
        </el-select>
        <div class="price-range">
          <el-input-number
            v-model="minPriceYuan"
            :min="0"
            :controls="false"
            placeholder="最低价"
            class="price-input"
          />
          <span class="price-sep">—</span>
          <el-input-number
            v-model="maxPriceYuan"
            :min="0"
            :controls="false"
            placeholder="最高价"
            class="price-input"
          />
          <span class="price-unit">元</span>
        </div>
        <el-button type="primary" @click="applyFilters">筛选</el-button>
        <el-button text @click="resetFilters">重置</el-button>
      </div>

      <div class="category-path" v-if="categoryPath.length">
        分类：{{ categoryPath.join(' / ') }}
        <el-button text size="small" @click="handleCategoryChange(null)">清除分类</el-button>
      </div>

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

/* ========== 筛选栏 ========== */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 6px;
}

.filter-category {
  width: 220px;
}

.filter-brand {
  width: 160px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.price-input {
  width: 110px;
}

.price-sep,
.price-unit {
  font-size: 12px;
  color: #999;
}

.category-path {
  margin-bottom: 10px;
  font-size: 13px;
  color: #666;
}

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