<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
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
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  padding: 20px 24px 12px;
}

.admin-menu {
  border-right: none;
}

.admin-main {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}
</style>
