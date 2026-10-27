<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Role } from '@/types/common'
import { getCategoryTree } from '@/api/product'
import type { Category } from '@/types/product'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const categories = ref<Category[]>([])

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '我的账户')

const isUser = computed(() => userStore.role === Role.USER)
const isMerchant = computed(() => userStore.role === Role.MERCHANT)
const isAdmin = computed(() => userStore.role === Role.ADMIN)

const onSearch = () => {
  const kw = keyword.value.trim()
  if (kw) {
    router.push({ path: '/search', query: { keyword: kw } })
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {
    categories.value = []
  }
}

const onCategoryClick = (cat: Category) => {
  router.push({ path: '/search', query: { categoryId: String(cat.id) } })
}

onMounted(() => {
  loadCategories()
})

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'orders':
      router.push('/orders')
      break
    case 'address':
      router.push('/address')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'merchant':
      router.push('/merchant/products')
      break
    case 'admin':
      router.push('/admin/dashboard')
      break
    case 'logout':
      userStore.logout()
      router.push('/home')
      break
  }
}
</script>

<template>
  <div class="mall-layout">
    <!-- 顶部导航栏 -->
    <header class="mall-header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/home')">
          <span class="logo-icon">🛒</span>
          <span class="logo-text">网上商城</span>
        </div>

        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          class="search-input"
          @keyup.enter="onSearch"
        >
          <template #append>
            <el-button @click="onSearch">搜索</el-button>
          </template>
        </el-input>

        <div class="header-actions">
          <!-- 购物车 -->
          <el-badge :value="0" :hidden="true" class="cart-badge">
            <el-button text class="nav-btn" @click="router.push('/cart')">
              🛒 购物车
            </el-button>
          </el-badge>

          <!-- 已登录 -->
          <template v-if="userStore.isLoggedIn">
            <!-- 商家入口 -->
            <el-button
              v-if="isMerchant"
              text
              class="nav-btn role-btn"
              @click="router.push('/merchant/products')"
            >
              🏪 卖家中心
            </el-button>

            <!-- 管理员入口 -->
            <el-button
              v-if="isAdmin"
              text
              class="nav-btn role-btn"
              @click="router.push('/admin/dashboard')"
            >
              ⚙ 管理后台
            </el-button>

            <!-- 用户菜单 -->
            <el-dropdown trigger="hover" @command="handleUserCommand">
              <span class="user-name">
                <span class="role-badge" :class="{
                  'badge-user': isUser,
                  'badge-merchant': isMerchant,
                  'badge-admin': isAdmin,
                }">
                  {{ isUser ? '买家' : isMerchant ? '商家' : '管理员' }}
                </span>
                {{ displayName }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders">📋 我的订单</el-dropdown-item>
                  <el-dropdown-item command="address">📍 收货地址</el-dropdown-item>
                  <el-dropdown-item command="profile">👤 个人信息</el-dropdown-item>
                  <el-dropdown-item
                    v-if="isMerchant"
                    command="merchant"
                    divided
                  >
                    🏪 卖家中心
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isAdmin"
                    command="admin"
                    divided
                  >
                    ⚙ 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 未登录 -->
          <div v-else class="auth-links">
            <el-button text class="nav-btn" @click="router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" class="register-btn" @click="router.push('/register')">
              免费注册
            </el-button>
          </div>
        </div>
      </div>
    </header>

    <!-- 分类导航栏 -->
    <nav class="category-bar">
      <div class="category-inner">
        <span class="category-item active" @click="router.push('/home')">全部商品</span>
        <span
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          @click="onCategoryClick(cat)"
        >
          {{ cat.name }}
        </span>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="mall-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="mall-footer">
      <div class="footer-inner">
        <div class="footer-columns">
          <div class="footer-col">
            <h4 class="footer-col-title">购物指南</h4>
            <p class="footer-link" @click="router.push('/help/shopping')">购物流程</p>
            <p class="footer-link" @click="router.push('/help/shopping')">会员介绍</p>
            <p class="footer-link" @click="router.push('/help/shopping')">常见问题</p>
          </div>
          <div class="footer-col">
            <h4 class="footer-col-title">配送方式</h4>
            <p class="footer-link" @click="router.push('/help/delivery')">配送范围</p>
            <p class="footer-link" @click="router.push('/help/delivery')">配送时间</p>
            <p class="footer-link" @click="router.push('/help/delivery')">验货与签收</p>
          </div>
          <div class="footer-col">
            <h4 class="footer-col-title">支付方式</h4>
            <p class="footer-link" @click="router.push('/help/payment')">在线支付</p>
            <p class="footer-link" @click="router.push('/help/payment')">分期付款</p>
            <p class="footer-link" @click="router.push('/help/payment')">发票制度</p>
          </div>
          <div class="footer-col">
            <h4 class="footer-col-title">售后服务</h4>
            <p class="footer-link" @click="router.push('/help/after-sale')">退换货政策</p>
            <p class="footer-link" @click="router.push('/help/after-sale')">退换货流程</p>
            <p class="footer-link" @click="router.push('/help/after-sale')">价格保护</p>
          </div>
        </div>
        <p class="footer-copy">© 2026 网上商城 版权所有 - 课程设计项目</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.mall-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

/* ========== 顶部导航栏 ========== */
.mall-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 22px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #e2231a;
  letter-spacing: 1px;
}

.search-input {
  flex: 1;
  max-width: 500px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.nav-btn {
  font-size: 14px;
  color: #333;
}

.role-btn {
  color: #e6a23c;
}

.role-btn:hover {
  color: #cf9236 !important;
  background: #fdf6ec !important;
}

.cart-badge {
  margin-right: 4px;
}

/* ========== 用户区域 ========== */
.user-name {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-name:hover {
  background: #f5f5f5;
}

.role-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  color: #fff;
  font-weight: 500;
  line-height: 18px;
}

.badge-user {
  background: #67c23a;
}

.badge-merchant {
  background: #e6a23c;
}

.badge-admin {
  background: #f56c6c;
}

/* ========== 未登录 ========== */
.auth-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.register-btn {
  border-radius: 16px;
}

/* ========== 分类导航栏 ========== */
.category-bar {
  background: #fff;
  border-bottom: 2px solid #e2231a;
}

.category-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  height: 40px;
  gap: 24px;
  overflow-x: auto;
}

.category-item {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  padding: 0 4px;
  height: 40px;
  line-height: 40px;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: color 0.2s;
}

.category-item:hover,
.category-item.active {
  color: #e2231a;
  border-bottom-color: #e2231a;
}

/* ========== 内容区域 ========== */
.mall-content {
  flex: 1;
  min-height: 400px;
}

/* ========== 页脚 ========== */
.mall-footer {
  background: #333;
  color: #999;
  padding: 30px 0;
  margin-top: 40px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-columns {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  text-align: left;
  margin-bottom: 24px;
}

.footer-col-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 12px;
}

.footer-link {
  font-size: 13px;
  margin: 0 0 8px;
  cursor: pointer;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #e2231a;
}

.footer-copy {
  font-size: 12px;
  margin: 0;
  padding-top: 20px;
  border-top: 1px solid #444;
}
</style>