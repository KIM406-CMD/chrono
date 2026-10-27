<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<template>
  <div class="merchant-layout">
    <aside class="merchant-aside">
      <div class="panel-title">商家中心</div>
      <el-menu router :default-active="route.path" class="merchant-menu">
        <el-menu-item index="/merchant/products">商品管理</el-menu-item>
        <el-menu-item index="/merchant/orders">订单管理</el-menu-item>
        <el-menu-item index="/merchant/refunds">退款审核</el-menu-item>
        <el-menu-item index="/merchant/stats">销售统计</el-menu-item>
      </el-menu>
      <div class="back-home">
        <el-button text @click="router.push('/home')">&larr; 返回首页</el-button>
      </div>
    </aside>
    <main class="merchant-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
}

.merchant-aside {
  width: 220px;
  border-right: 1px solid var(--el-border-color-light);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  padding: 20px 24px 12px;
}

.merchant-menu {
  border-right: none;
  flex: 1;
}

.back-home {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-light);
}

.merchant-main {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}
</style>