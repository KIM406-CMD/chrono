<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="nf-page">
    <div class="nf-content">
      <h1 class="nf-code">404</h1>
      <h2 class="nf-title">页面未找到</h2>
      <p class="nf-desc">您访问的页面不存在或已被移除</p>
      <div class="nf-actions">
        <el-button type="primary" size="large" @click="router.push('/home')">返回首页</el-button>
        <el-button size="large" @click="router.back()">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nf-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.nf-content {
  text-align: center;
}

.nf-code {
  font-size: 120px;
  font-weight: 700;
  color: #e2231a;
  margin: 0;
  line-height: 1;
  letter-spacing: -4px;
}

.nf-title {
  font-size: 28px;
  color: #333;
  margin: 16px 0 8px;
}

.nf-desc {
  font-size: 15px;
  color: #999;
  margin: 0 0 32px;
}

.nf-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>