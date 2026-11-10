<script setup lang="ts">
import { onMounted } from 'vue'
import { getUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 启动时刷新用户信息，避免使用 localStorage 中过期的昵称/角色缓存
onMounted(async () => {
  if (!userStore.token) return
  try {
    const res = await getUserInfo()
    if (res.data) userStore.setUserInfo(res.data)
  } catch {
    // 登录态失效由 Axios 拦截器统一处理，其余异常保留本地缓存
  }
})
</script>

<template>
  <router-view />
</template>
