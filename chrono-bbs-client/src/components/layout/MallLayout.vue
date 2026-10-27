<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

const displayName = computed(() => userStore.userInfo?.username ?? '我的账户')

const onSearch = () => {
  const kw = keyword.value.trim()
  if (kw) {
    router.push({ path: '/search', query: { keyword: kw } })
  }
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/home')
  }
}
</script>

<template>
  <div class="mall-layout">
    <header class="mall-header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/home')">网上超市</div>
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
          <el-button text @click="router.push('/cart')">购物车</el-button>
          <el-dropdown v-if="userStore.isLoggedIn" @command="handleUserCommand">
            <span class="user-name">{{ displayName }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item @click="router.push('/address')">收货地址</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div v-else class="auth-links">
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button text @click="router.push('/register')">注册</el-button>
          </div>
        </div>
      </div>
    </header>
    <nav class="category-bar">
      <div class="category-inner">全部分类</div>
    </nav>
    <main class="mall-content">
      <router-view />
    </main>
    <footer class="mall-footer">© 2026 网上超市 版权所有</footer>
  </div>
</template>

<style scoped>
.mall-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 16px 24px;
  box-sizing: border-box;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.search-input {
  max-width: 480px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.user-name {
  cursor: pointer;
  padding: 0 8px;
  color: var(--el-text-primary);
}

.category-bar {
  border-bottom: 1px solid var(--el-border-color-light);
}

.category-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 24px;
  font-weight: 500;
}

.mall-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}

.mall-footer {
  text-align: center;
  padding: 16px;
  color: var(--el-text-secondary);
  font-size: 12px;
}
</style>
