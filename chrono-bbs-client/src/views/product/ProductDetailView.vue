<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Plus, Minus, Shop, ShoppingCart } from '@element-plus/icons-vue'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { getComments } from '@/api/comment'
import { createConversation } from '@/api/chat'
import type { Product, Sku } from '@/types/product'
import type { CommentItem } from '@/types/comment'
import { useUserStore } from '@/stores/user'
import { sanitizeHtml } from '@/utils/sanitizeHtml'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const productId = computed(() => Number(route.params.id))
const product = ref<Product | null>(null)
const loading = ref(false)
const currentImage = ref(0)
const quantity = ref(1)
const selectedSku = ref<Sku | null>(null)

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(productId.value)
    product.value = res.data
    if (res.data.skuList && res.data.skuList.length > 0) {
      selectedSku.value = res.data.skuList[0]
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 切换图片
const prevImage = () => {
  if (product.value && product.value.images.length > 0) {
    currentImage.value = (currentImage.value - 1 + product.value.images.length) % product.value.images.length
  }
}

const nextImage = () => {
  if (product.value && product.value.images.length > 0) {
    currentImage.value = (currentImage.value + 1) % product.value.images.length
  }
}

// 数量加减
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const increaseQuantity = () => {
  const maxStock = selectedSku.value?.stock || 999
  if (quantity.value < maxStock) {
    quantity.value++
  }
}

// 选择SKU
const selectSku = (sku: Sku) => {
  selectedSku.value = sku
  quantity.value = 1
}

// 加入购物车
const handleAddToCart = async () => {
  if (!selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  try {
    await addToCart({
      productId: productId.value,
      skuId: selectedSku.value.id,
      quantity: quantity.value,
    })
    ElMessage.success('已加入购物车')
  } catch (error: any) {
    ElMessage.error(error.message || '加入购物车失败')
  }
}

// 立即购买：后端创建订单基于 cartItemIds，故先加入购物车再进结算页
const handleBuyNow = async () => {
  if (!selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  try {
    await addToCart({
      productId: productId.value,
      skuId: selectedSku.value.id,
      quantity: quantity.value,
    })
    router.push('/checkout')
  } catch (error: any) {
    ElMessage.error(error.message || '加入购物车失败')
  }
}

// 价格格式化（分转元）
const formatPrice = (price: number) => {
  return (price / 100).toFixed(2)
}

// 从商品详情发起与商家的会话
const handleContactShop = async () => {
  if (!product.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再联系客服')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await createConversation({
      // 聊天会话使用商家用户 ID；Mock 数据未提供时回退到 merchantId
      merchantId: product.value.merchantUserId ?? product.value.merchantId,
      productId: product.value.id,
    })
    router.push(`/chat/${res.data.id}`)
  } catch {
    ElMessage.error('创建会话失败')
  }
}

// 商品详情富文本（清洗后渲染，避免 XSS）
const sanitizedDescription = computed(() => sanitizeHtml(product.value?.description || ''))

// 顶部副标题使用去标签后的纯文本
const plainDescription = computed(() =>
  (product.value?.description || '').replace(/<[^>]*>/g, '').trim(),
)

// 评论相关
const activeTab = ref<'detail' | 'specs' | 'comments'>('detail')
const comments = ref<CommentItem[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentSize = 10
const commentStar = ref<number | ''>('')
const commentLoading = ref(false)

const loadComments = async () => {
  commentLoading.value = true
  try {
    const params: any = { page: commentPage.value, size: commentSize }
    if (commentStar.value !== '') {
      params.star = commentStar.value
    }
    const res = await getComments(productId.value, params)
    comments.value = res.data.list || []
    commentTotal.value = res.data.total
  } catch {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

const handleCommentPageChange = (page: number) => {
  commentPage.value = page
  loadComments()
}

const handleStarFilter = (star: number | '') => {
  commentStar.value = star
  commentPage.value = 1
  loadComments()
}

const switchTab = (tab: 'detail' | 'specs' | 'comments') => {
  activeTab.value = tab
  if (tab === 'comments' && comments.value.length === 0) {
    loadComments()
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-bar">
      <div class="breadcrumb-content">
        <router-link to="/home" class="breadcrumb-link">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <span class="breadcrumb-current">{{ product?.name }}</span>
      </div>
    </div>

    <div class="product-detail-container">
      <!-- 商品主信息区 -->
      <div class="product-main">
        <!-- 左侧图片区 -->
        <div class="product-gallery">
          <div class="main-image-wrapper">
            <div class="main-image">
              <img v-if="product?.images && product.images[currentImage]" :src="product.images[currentImage]" :alt="product.name" />
              <div v-else class="image-placeholder">
                <span>暂无图片</span>
              </div>
            </div>
            <div class="image-nav" v-if="product?.images && product.images.length > 1">
              <button class="nav-btn prev-btn" @click="prevImage">
                <ArrowLeft />
              </button>
              <button class="nav-btn next-btn" @click="nextImage">
                <ArrowRight />
              </button>
            </div>
          </div>
          
          <!-- 缩略图列表 -->
          <div class="thumbnail-list" v-if="product?.images && product.images.length > 1">
            <div
              v-for="(img, index) in product.images"
              :key="index"
              :class="['thumbnail-item', { active: currentImage === index }]"
              @click="currentImage = index"
            >
              <img :src="img" :alt="`缩略图${index + 1}`" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区 -->
        <div class="product-info">
          <!-- 商品标题 -->
          <h1 class="product-title">{{ product?.name }}</h1>
          
          <!-- 商品简介 -->
          <p class="product-subtitle">{{ plainDescription }}</p>

          <!-- 价格区 -->
          <div class="price-section">
            <div class="price-row">
              <span class="price-label">价格</span>
              <div class="price-value">
                <span class="price-symbol">¥</span>
                <span class="price-number">{{ formatPrice(selectedSku?.price || 0) }}</span>
              </div>
            </div>
            <div class="price-row">
              <span class="price-label">促销</span>
              <div class="promotion-tag">
                <span class="promo-badge">限时特惠</span>
                <span class="promo-text">满100减20，满200减50</span>
              </div>
            </div>
          </div>

          <!-- SKU选择 -->
          <div class="sku-section" v-if="product?.skuList && product.skuList.length > 0">
            <div class="sku-label">规格</div>
            <div class="sku-options">
              <div
                v-for="sku in product.skuList"
                :key="sku.id"
                :class="['sku-item', { selected: selectedSku?.id === sku.id }]"
                @click="selectSku(sku)"
              >
                {{ sku.specText || `规格${sku.id}` }}
              </div>
            </div>
          </div>

          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="quantity-label">数量</span>
            <div class="quantity-control">
              <button class="qty-btn" @click="decreaseQuantity" :disabled="quantity <= 1">
                <Minus />
              </button>
              <input type="number" v-model.number="quantity" class="qty-input" min="1" />
              <button class="qty-btn" @click="increaseQuantity">
                <Plus />
              </button>
              <span class="stock-info">库存 {{ selectedSku?.stock ?? 0 }} 件</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section">
            <button class="action-btn cart-btn" @click="handleAddToCart">
              <ShoppingCart />
              <span>加入购物车</span>
            </button>
            <button class="action-btn buy-btn" @click="handleBuyNow">
              <span>立即购买</span>
            </button>
          </div>

          <!-- 店铺信息 -->
          <div class="shop-section">
            <div class="shop-info">
              <Shop class="shop-icon" />
              <div class="shop-text">
                <span class="shop-name">{{ product?.shopName }}</span>
                <span class="shop-desc">官方认证店铺</span>
              </div>
            </div>
            <el-button plain type="primary" size="small" @click="handleContactShop">
              联系客服
            </el-button>
          </div>

          <!-- 商品参数 -->
          <div class="params-section">
            <div class="param-item">
              <span class="param-label">品牌</span>
              <span class="param-value">{{ product?.brand }}</span>
            </div>
            <div class="param-item">
              <span class="param-label">分类</span>
              <span class="param-value">{{ product?.categoryName }}</span>
            </div>
            <div class="param-item">
              <span class="param-label">销量</span>
              <span class="param-value">{{ product?.sales || 0 }} 件</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品详情描述区 -->
      <div class="product-desc-section">
        <div class="desc-tabs">
          <div :class="['desc-tab', { active: activeTab === 'detail' }]" @click="switchTab('detail')">商品详情</div>
          <div :class="['desc-tab', { active: activeTab === 'specs' }]" @click="switchTab('specs')">规格参数</div>
          <div :class="['desc-tab', { active: activeTab === 'comments' }]" @click="switchTab('comments')">用户评价 ({{ commentTotal }})</div>
        </div>
        
        <div class="desc-content">
          <template v-if="activeTab === 'detail'">
            <div v-if="sanitizedDescription" class="desc-rich" v-html="sanitizedDescription"></div>
            <div v-else class="desc-placeholder">
              <p>暂无商品详情</p>
              <p class="desc-hint">商家暂未填写图文描述</p>
            </div>
          </template>

          <template v-else-if="activeTab === 'specs'">
            <div class="spec-params">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="品牌">{{ product?.brand || '—' }}</el-descriptions-item>
                <el-descriptions-item label="分类">{{ product?.categoryName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="店铺">{{ product?.shopName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="销量">{{ product?.sales || 0 }} 件</el-descriptions-item>
              </el-descriptions>
              <el-table
                v-if="product?.skuList?.length"
                :data="product.skuList"
                size="small"
                border
                class="spec-table"
              >
                <el-table-column prop="specText" label="规格" min-width="160" />
                <el-table-column label="价格" width="120" align="right">
                  <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="100" align="center" />
              </el-table>
            </div>
          </template>

          <template v-else-if="activeTab === 'comments'">
            <div class="comment-section" v-loading="commentLoading">
              <!-- 评价入口位于订单详情，仅已收货订单可评价 -->
              <p class="comment-tip">
                {{ userStore.isLoggedIn ? '发表评价请前往「我的订单」→ 订单详情' : '登录后可发表评价' }}
              </p>

              <!-- 星级筛选 -->
              <div class="comment-filter">
                <span
                  :class="['star-filter-item', { active: commentStar === '' }]"
                  @click="handleStarFilter('')"
                >全部</span>
                <span
                  v-for="s in 5"
                  :key="s"
                  :class="['star-filter-item', { active: commentStar === s }]"
                  @click="handleStarFilter(s)"
                >{{ s }}星</span>
              </div>

              <!-- 评论列表 -->
              <div v-if="comments.length === 0" class="comment-empty">
                <p>暂无评论，快来发表第一条评论吧</p>
              </div>

              <div v-for="item in comments" :key="item.id" class="comment-item">
                <div class="comment-avatar">
                  <el-avatar :size="40" :src="item.avatar">{{ (item.nickname || item.username)?.charAt(0) }}</el-avatar>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-user">{{ item.nickname || item.username }}</span>
                    <el-rate :model-value="item.star" disabled show-score size="small" />
                    <span class="comment-time">{{ item.createdAt }}</span>
                  </div>
                  <p class="comment-text">{{ item.content }}</p>
                  <div v-if="item.images?.length" class="comment-images">
                    <el-image
                      v-for="(img, index) in item.images"
                      :key="index"
                      :src="img"
                      fit="cover"
                      class="comment-image"
                      :preview-src-list="item.images"
                      :initial-index="index"
                    />
                  </div>
                  <div v-if="item.append" class="comment-append">
                    <span class="append-label">追评：</span>{{ item.append.content }}
                    <span class="append-time">（{{ item.append.createdAt }}）</span>
                  </div>
                  <div v-for="reply in item.replies || []" :key="reply.id" class="comment-reply">
                    <span class="reply-label">{{ reply.nickname || reply.username || '商家' }}回复：</span>{{ reply.content }}
                    <span class="reply-time">（{{ reply.createdAt }}）</span>
                  </div>
                </div>
              </div>

              <!-- 分页 -->
              <div class="comment-pagination" v-if="commentTotal > commentSize">
                <el-pagination
                  v-model:current-page="commentPage"
                  :page-size="commentSize"
                  :total="commentTotal"
                  layout="prev, pager, next"
                  @current-change="handleCommentPageChange"
                />
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 面包屑导航 */
.breadcrumb-bar {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.breadcrumb-content {
  max-width: var(--content-width);
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-link {
  color: #666;
  text-decoration: none;
}

.breadcrumb-link:hover {
  color: #e2231a;
}

.breadcrumb-sep {
  color: #999;
}

.breadcrumb-current {
  color: #333;
}

/* 商品详情容器 */
.product-detail-container {
  max-width: var(--content-width);
  margin: 24px auto;
  padding: 0 24px;
}

/* 商品主信息区 */
.product-main {
  display: grid;
  grid-template-columns: 500px 1fr;
  gap: 32px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 图片区 */
.product-gallery {
  position: sticky;
  top: 24px;
}

.main-image-wrapper {
  position: relative;
  aspect-ratio: 1;
  background: #f9fafb;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%);
  color: #999;
  font-size: 18px;
}

.image-nav {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  padding: 0 12px;
  pointer-events: none;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-btn:hover {
  background: #fff;
  transform: scale(1.1);
}

/* 缩略图 */
.thumbnail-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
  flex-shrink: 0;
}

.thumbnail-item:hover {
  border-color: #e2231a;
}

.thumbnail-item.active {
  border-color: #e2231a;
  box-shadow: 0 0 0 2px rgba(226, 35, 26, 0.2);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 右侧信息区 */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 价格区 */
.price-section {
  background: #fff5f5;
  border-radius: 12px;
  padding: 20px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.price-label {
  font-size: 14px;
  color: #666;
  min-width: 60px;
}

.price-value {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 18px;
  color: #e2231a;
  font-weight: 600;
}

.price-number {
  font-size: 32px;
  color: #e2231a;
  font-weight: 700;
}

.price-range {
  font-size: 14px;
  color: #999;
  margin-left: 8px;
}

.promotion-tag {
  display: flex;
  align-items: center;
  gap: 12px;
}

.promo-badge {
  background: #e2231a;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.promo-text {
  font-size: 14px;
  color: #e2231a;
}

/* SKU选择 */
.sku-section {
  padding: 16px 0;
}

.sku-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sku-item {
  padding: 10px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.sku-item:hover {
  border-color: #e2231a;
}

.sku-item.selected {
  border-color: #e2231a;
  background: #fff5f5;
  color: #e2231a;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
  min-width: 60px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.qty-btn:hover:not(:disabled) {
  border-color: #e2231a;
  color: #e2231a;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
}

.stock-info {
  font-size: 13px;
  color: #999;
  margin-left: 12px;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 12px;
  padding: 8px 0;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.cart-btn {
  background: #ffedeb;
  color: #e2231a;
  border: 1px solid #e2231a;
}

.cart-btn:hover {
  background: #e2231a;
  color: #fff;
}

.buy-btn {
  background: #e2231a;
  color: #fff;
}

.buy-btn:hover {
  background: #c91b14;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(226, 35, 26, 0.3);
}

.icon-btn {
  width: 48px;
  padding: 14px;
  background: #f5f5f5;
  color: #666;
}

.icon-btn:hover {
  background: #e5e7eb;
  color: #e2231a;
}

/* 店铺信息 */
.shop-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-icon {
  font-size: 24px;
  color: #e2231a;
}

.shop-text {
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.shop-desc {
  font-size: 12px;
  color: #999;
}

.shop-link {
  color: #e2231a;
  text-decoration: none;
  font-size: 14px;
}

.shop-link:hover {
  text-decoration: underline;
}

/* 商品参数 */
.params-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 16px 0;
  border-top: 1px solid #e5e7eb;
}

.param-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.param-label {
  font-size: 12px;
  color: #999;
}

.param-value {
  font-size: 14px;
  color: #333;
}

/* 商品详情描述区 */
.product-desc-section {
  background: #fff;
  border-radius: 12px;
  margin-top: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.desc-tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
}

.desc-tab {
  padding: 20px 32px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.desc-tab:hover {
  color: #e2231a;
}

.desc-tab.active {
  color: #e2231a;
  border-bottom-color: #e2231a;
  font-weight: 500;
}

.desc-content {
  padding: 32px;
  min-height: 400px;
}

.desc-placeholder {
  text-align: center;
  color: #999;
  padding: 60px 0;
}

.desc-placeholder p {
  margin: 8px 0;
}

.desc-rich {
  font-size: 14px;
  line-height: 1.9;
  color: #444;
  word-break: break-word;
}

.desc-rich :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px 0;
}

.desc-rich :deep(p) {
  margin: 8px 0;
}

.spec-params {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.spec-table {
  width: 100%;
}

.desc-hint {
  font-size: 14px;
  color: #bbb;
}

/* 评论区域 */
.comment-section {
  padding: 24px 0;
}

.comment-tip {
  margin: 0 0 20px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 13px;
  color: #999;
}

.comment-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.star-filter-item {
  padding: 4px 16px;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;
}

.star-filter-item:hover {
  color: #e2231a;
}

.star-filter-item.active {
  color: #fff;
  background: #e2231a;
}

.comment-empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.comment-image {
  width: 72px;
  height: 72px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
}

.comment-append {
  margin-top: 10px;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.append-label {
  color: #e2231a;
  font-weight: 500;
}

.append-time {
  color: #999;
  font-size: 12px;
}

.comment-reply {
  margin-top: 10px;
  padding: 10px 14px;
  background: #fff8f0;
  border-radius: 6px;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.reply-label {
  color: #e6a23c;
  font-weight: 500;
}

.reply-time {
  color: #999;
  font-size: 12px;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .product-main {
    grid-template-columns: 1fr;
  }
  
  .product-gallery {
    position: static;
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 0 16px;
  }
  
  .product-main {
    padding: 20px;
  }
  
  .action-section {
    flex-wrap: wrap;
  }
  
  .cart-btn, .buy-btn {
    flex: 1;
  }
  
  .params-section {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>