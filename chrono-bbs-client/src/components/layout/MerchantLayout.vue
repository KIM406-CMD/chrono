<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
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
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  padding: 20px 24px 12px;
}

.merchant-menu {
  border-right: none;
}

.merchant-main {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}
</style>
