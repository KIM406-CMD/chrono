<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-aside">
      <div class="panel-title">管理后台</div>
      <el-menu router :default-active="route.path" class="admin-menu">
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants">商家审核</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/report">销售报表</el-menu-item>
      </el-menu>
      <div class="back-home">
        <el-button text @click="router.push('/home')">&larr; 返回首页</el-button>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-aside {
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

.admin-menu {
  border-right: none;
  flex: 1;
}

.back-home {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-light);
}

.admin-main {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}
</style>