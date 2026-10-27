<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Plus, Minus, Shop, Share, Star, ShoppingCart } from '@element-plus/icons-vue'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import type { Product, Sku } from '@/types/product'

const route = useRoute()
const router = useRouter()

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
    if (res.data.skus && res.data.skus.length > 0) {
      selectedSku.value = res.data.skus[0]
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

// 立即购买
const handleBuyNow = async () => {
  if (!selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  router.push({
    path: '/checkout',
    query: {
      productId: productId.value,
      skuId: selectedSku.value.id,
      quantity: quantity.value,
    },
  })
}

// 价格格式化（分转元）
const formatPrice = (price: number) => {
  return (price / 100).toFixed(2)
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
          <p class="product-subtitle">{{ product?.description }}</p>

          <!-- 价格区 -->
          <div class="price-section">
            <div class="price-row">
              <span class="price-label">价格</span>
              <div class="price-value">
                <span class="price-symbol">¥</span>
                <span class="price-number">{{ formatPrice(selectedSku?.price || product?.minPrice || 0) }}</span>
                <span class="price-range" v-if="product && product.minPrice !== product.maxPrice">
                  - ¥{{ formatPrice(product.maxPrice) }}
                </span>
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
          <div class="sku-section" v-if="product?.skus && product.skus.length > 0">
            <div class="sku-label">规格</div>
            <div class="sku-options">
              <div
                v-for="sku in product.skus"
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
              <span class="stock-info">库存 {{ selectedSku?.stock || product?.sales || 0 }} 件</span>
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
            <button class="action-btn icon-btn" title="收藏">
              <Star />
            </button>
            <button class="action-btn icon-btn" title="分享">
              <Share />
            </button>
          </div>

          <!-- 店铺信息 -->
          <div class="shop-section">
            <div class="shop-info">
              <Shop class="shop-icon" />
              <div class="shop-text">
                <span class="shop-name">{{ product?.merchantName }}</span>
                <span class="shop-desc">官方认证店铺</span>
              </div>
            </div>
            <router-link :to="`/shop/${product?.merchantId}`" class="shop-link">进店逛逛</router-link>
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
          <div class="desc-tab active">商品详情</div>
          <div class="desc-tab">规格参数</div>
          <div class="desc-tab">用户评价</div>
        </div>
        
        <div class="desc-content">
          <div class="desc-placeholder">
            <p>商品详情内容</p>
            <p class="desc-hint">此处将展示商品的详细描述、规格参数、用户评价等信息</p>
          </div>
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
  max-width: 1200px;
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
  max-width: 1200px;
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

.desc-hint {
  font-size: 14px;
  color: #bbb;
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