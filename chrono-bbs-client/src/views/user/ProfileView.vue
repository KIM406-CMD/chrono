<script setup lang="ts">
import { reactive, ref } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'

const activeTab = ref<'info' | 'password'>('info')

const form = reactive({
  nickname: '',
  email: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// Image upload placeholder
const avatar = ref('')

const handleUpdateProfile = async () => {
  // Placeholder - will call updateProfile API
  console.log('update profile', form)
}

const handleUpdatePassword = async () => {
  // Placeholder - will call updatePassword API
  console.log('update password', passwordForm)
}
</script>

<template>
  <div class="pf-page">
    <h2>个人信息</h2>

    <div class="pf-card">
      <!-- Avatar -->
      <div class="avatar-section">
        <el-avatar :size="80" :src="avatar" class="pf-avatar">
          <el-icon :size="40"><UserFilled /></el-icon>
        </el-avatar>
        <div class="avatar-info">
          <p class="username-display">我的账户</p>
          <p class="avatar-hint">支持 JPG/PNG 格式，不超过 2MB</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="pf-tabs">
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="form" label-width="80px" class="pf-form">
            <el-form-item label="用户名">
              <el-input disabled value="user" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input disabled value="138****0001" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>买家</el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" label-width="100px" class="pf-form">
            <el-form-item label="原密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.pf-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.pf-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 20px;
  color: #333;
}

.pf-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.pf-avatar {
  border: 2px solid #eee;
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username-display {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.avatar-hint {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.pf-tabs {
  margin-top: 4px;
}

.pf-form {
  max-width: 400px;
  padding-top: 8px;
}
</style>