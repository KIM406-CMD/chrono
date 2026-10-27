<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import { getProductList, getCategoryTree } from '@/api/product'
import { useUserStore } from '@/stores/user'
import type { Product, Category } from '@/types/product'
import type { PageResult } from '@/types/common'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const searchKeyword = ref('')

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '用户')

// 获取分类树
const fetchCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data
  } catch (error) {
    // Mock 模式
  }
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getProductList({ page: 1, size: 20 })
    products.value = res.data.list
  } catch (error) {
    // Mock 模式
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

// 进入商品详情
const goToProduct = (id: number) => {
  router.push(`/product/${id}`)
}

// 价格格式化（分转元）
const formatPrice = (price: number) => {
  return (price / 100).toFixed(2)
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<template>
  <div class="home-page">
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="container">
        <!-- Banner 轮播区 -->
        <section class="banner-section">
          <div class="banner-left">
            <div class="category-sidebar">
              <ul class="category-list">
                <li v-for="cat in categories.slice(0, 8)" :key="cat.id" class="category-item">
                  <span class="cat-icon">📦</span>
                  <span class="cat-name">{{ cat.name }}</span>
                  <ArrowRight class="cat-arrow" />
                </li>
              </ul>
            </div>
            <div class="banner-carousel">
              <div class="banner-placeholder">
                <div class="banner-text">
                  <h2>限时特惠</h2>
                  <p>精选好物 低至1折</p>
                </div>
              </div>
            </div>
          </div>
          <div class="banner-right">
            <div class="user-card">
              <template v-if="userStore.isLoggedIn">
                <div class="user-avatar">👤</div>
                <p class="user-tip">Hi，{{ displayName }}</p>
                <div class="user-actions">
                  <router-link to="/orders" class="btn-login">我的订单</router-link>
                  <router-link to="/cart" class="btn-register">购物车</router-link>
                </div>
              </template>
              <template v-else>
                <div class="user-avatar">👤</div>
                <p class="user-tip">Hi，欢迎来到网上商城</p>
                <div class="user-actions">
                  <router-link to="/login" class="btn-login">登录</router-link>
                  <router-link to="/register" class="btn-register">注册</router-link>
                </div>
              </template>
            </div>
            <div class="news-card">
              <h4 class="news-title">商城公告</h4>
              <ul class="news-list">
                <li class="news-item">
                  <span class="news-tag">活动</span>
                  <span class="news-text">新人注册立享优惠</span>
                </li>
                <li class="news-item">
                  <span class="news-tag">通知</span>
                  <span class="news-text">春节不打烊公告</span>
                </li>
                <li class="news-item">
                  <span class="news-tag">活动</span>
                  <span class="news-text">满减活动进行中</span>
                </li>
              </ul>
            </div>
          </div>
        </section>

        <!-- 热销商品 -->
        <section class="product-section">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">🔥</span>
              热销推荐
            </h3>
            <router-link to="/search" class="section-more">
              查看更多 <ArrowRight />
            </router-link>
          </div>
          
          <div v-loading="loading" class="product-grid">
            <div
              v-for="product in products.slice(0, 10)"
              :key="product.id"
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <div class="product-image">
                <div class="image-placeholder">
                  <span class="placeholder-icon">📷</span>
                  <span class="placeholder-text">暂无图片</span>
                </div>
              </div>
              <div class="product-info">
                <h4 class="product-name">{{ product.name }}</h4>
                <div class="product-tags">
                  <span class="product-tag">新品</span>
                  <span v-if="product.sales > 100" class="product-tag hot">热销</span>
                </div>
                <div class="product-price">
                  <span class="price-current">¥{{ formatPrice(product.minPrice) }}</span>
                  <span class="price-original">¥{{ formatPrice(product.maxPrice) }}</span>
                </div>
                <div class="product-sales">
                  <span>{{ product.sales }}+ 人付款</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 更多商品 -->
        <section class="product-section">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">✨</span>
              猜你喜欢
            </h3>
          </div>
          
          <div v-loading="loading" class="product-grid">
            <div
              v-for="product in products.slice(10, 20)"
              :key="product.id"
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <div class="product-image">
                <div class="image-placeholder">
                  <span class="placeholder-icon">📷</span>
                  <span class="placeholder-text">暂无图片</span>
                </div>
              </div>
              <div class="product-info">
                <h4 class="product-name">{{ product.name }}</h4>
                <div class="product-tags">
                  <span class="product-tag">特惠</span>
                </div>
                <div class="product-price">
                  <span class="price-current">¥{{ formatPrice(product.minPrice) }}</span>
                  <span class="price-original">¥{{ formatPrice(product.maxPrice) }}</span>
                </div>
                <div class="product-sales">
                  <span>{{ product.sales }}+ 人付款</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航 */
.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-top {
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 70px;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  color: #e2231a;
}

.search-box {
  flex: 1;
  max-width: 500px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px #e2231a inset;
}

.search-input :deep(.el-button) {
  background: #e2231a;
  border-color: #e2231a;
  color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}

.action-item:hover {
  color: #e2231a;
}

.action-icon {
  font-size: 18px;
}

/* 分类导航 */
.category-nav {
  background: #fff;
}

.nav-list {
  display: flex;
  gap: 32px;
  padding: 12px 0;
}

.nav-item {
  position: relative;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 0;
}

.nav-item:hover {
  color: #e2231a;
}

.submenu {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  padding: 8px 0;
  min-width: 120px;
  z-index: 10;
}

.nav-item:hover .submenu {
  display: block;
}

.submenu li {
  padding: 8px 16px;
  font-size: 13px;
  color: #666;
}

.submenu li:hover {
  background: #f5f5f5;
  color: #e2231a;
}

/* 主内容 */
.main-content {
  padding: 20px 0;
}

/* Banner 区域 */
.banner-section {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
  margin-bottom: 24px;
}

.banner-left {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.category-sidebar {
  background: #fff;
  border-right: 1px solid #f0f0f0;
}

.category-list {
  padding: 8px 0;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.category-item:hover {
  background: #fff5f5;
  color: #e2231a;
}

.cat-icon {
  font-size: 16px;
}

.cat-name {
  flex: 1;
}

.cat-arrow {
  width: 14px;
  height: 14px;
  opacity: 0;
  transition: opacity 0.2s;
}

.category-item:hover .cat-arrow {
  opacity: 1;
}

.banner-carousel {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-placeholder {
  text-align: center;
  color: #fff;
}

.banner-text h2 {
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 12px;
}

.banner-text p {
  font-size: 18px;
  opacity: 0.9;
}

.banner-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
}

.user-avatar {
  font-size: 48px;
  margin-bottom: 12px;
}

.user-tip {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.user-actions {
  display: flex;
  gap: 12px;
}

.btn-login, .btn-register {
  flex: 1;
  padding: 10px 0;
  border-radius: 20px;
  font-size: 14px;
  text-align: center;
  text-decoration: none;
  transition: all 0.2s;
}

.btn-login {
  background: #e2231a;
  color: #fff;
}

.btn-login:hover {
  background: #f53b32;
}

.btn-register {
  background: #fff;
  color: #e2231a;
  border: 1px solid #e2231a;
}

.btn-register:hover {
  background: #fff5f5;
}

.news-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.news-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.news-tag {
  background: #e2231a;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.news-text {
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 商品区块 */
.product-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.title-icon {
  font-size: 22px;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 14px;
  text-decoration: none;
}

.section-more:hover {
  color: #e2231a;
}

.section-more svg {
  width: 16px;
  height: 16px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.product-image {
  aspect-ratio: 1;
  background: #f5f5f5;
}

.image-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.placeholder-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.placeholder-text {
  font-size: 12px;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 400;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 40px;
}

.product-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.product-tag {
  background: #f5f5f5;
  color: #666;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-tag.hot {
  background: #fff5f5;
  color: #e2231a;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.price-current {
  color: #e2231a;
  font-size: 18px;
  font-weight: 600;
}

.price-current::before {
  content: '¥';
  font-size: 12px;
  margin-right: 2px;
}

.price-original {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}

.product-sales {
  color: #999;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .banner-section {
    grid-template-columns: 1fr;
  }
  
  .banner-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .banner-left {
    grid-template-columns: 1fr;
  }
  
  .category-sidebar {
    display: none;
  }
}
</style>