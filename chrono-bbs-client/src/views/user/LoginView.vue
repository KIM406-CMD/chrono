<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { login, getCaptcha } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/types/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const captchaUrl = ref('')
const captchaUuid = ref('')

const loginForm = reactive<LoginRequest>({
  username: '',
  password: '',
  captchaUuid: '',
  captchaCode: '',
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位字符', trigger: 'blur' },
  ],
})

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaUuid.value = res.data.uuid
    captchaUrl.value = res.data.image || ''
    loginForm.captchaUuid = res.data.uuid
  } catch (error) {
    captchaUrl.value = ''
  }
}

// 登录
const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    loginForm.captchaUuid = captchaUuid.value
    const res = await login(loginForm)
    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      id: res.data.userInfo.id,
      username: res.data.userInfo.username,
      nickname: res.data.userInfo.nickname,
      phone: res.data.userInfo.phone,
      email: '',
      avatar: '',
      role: res.data.userInfo.role,
      status: 1,
    })
    ElMessage.success('登录成功')
    
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
    fetchCaptcha()
    loginForm.captchaCode = ''
  } finally {
    loading.value = false
  }
}

// 回车登录
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleLogin()
  }
}

onMounted(() => {
  fetchCaptcha()
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌展示区 -->
    <div class="login-banner">
      <div class="banner-content">
        <div class="brand-logo">
          <div class="logo-icon">🛒</div>
          <h1 class="brand-name">网上商城</h1>
          <p class="brand-slogan">品质生活，从这里开始</p>
        </div>
        
        <div class="banner-features">
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <div class="feature-text">
              <h3>精选好物</h3>
              <p>严选优质商品，品质保证</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🚀</div>
            <div class="feature-text">
              <h3>极速配送</h3>
              <p>下单即发，快速送达</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">💰</div>
            <div class="feature-text">
              <h3>超值优惠</h3>
              <p>每日特价，省钱更省心</p>
            </div>
          </div>
        </div>
        
        <div class="banner-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
          <div class="deco-circle deco-3"></div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-form-section">
      <!-- 顶部导航 -->
      <header class="form-header">
        <router-link to="/" class="header-logo">
          <span class="logo-text">网上商城</span>
        </router-link>
        <div class="header-help">
          <span>还没有账号？</span>
          <router-link to="/register" class="link-register">立即注册</router-link>
        </div>
      </header>

      <!-- 登录卡片 -->
      <main class="login-main">
        <div class="login-card">
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p class="card-subtitle">登录您的账号以继续</p>
          </div>

          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.captchaCode"
                  placeholder="验证码"
                  size="large"
                  :prefix-icon="Key"
                  maxlength="4"
                  class="captcha-input"
                />
                <div class="captcha-box" @click="fetchCaptcha">
                  <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
                  <span v-else class="captcha-hint">点击刷新</span>
                </div>
              </div>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <router-link to="/forgot-password" class="link-forgot">忘记密码？</router-link>
            </div>

            <el-form-item class="form-submit">
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-divider">
            <span>其他登录方式</span>
          </div>

          <div class="other-methods">
            <el-button text class="method-btn">
              <span class="method-icon">📱</span>
              <span>手机号登录</span>
            </el-button>
          </div>
        </div>

        <div class="login-footer">
          <p>© 2025 网上商城系统 - 课程设计项目</p>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: #fff;
}

/* 左侧品牌展示区 */
.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.banner-content {
  position: relative;
  z-index: 2;
  max-width: 500px;
}

.brand-logo {
  margin-bottom: 60px;
}

.logo-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.brand-name {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.banner-features {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(10px);
}

.feature-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.feature-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 6px 0;
}

.feature-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 装饰元素 */
.banner-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.deco-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.deco-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
}

.deco-3 {
  width: 150px;
  height: 150px;
  bottom: 100px;
  right: 50px;
}

/* 右侧登录表单区 */
.login-form-section {
  flex: 1;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 48px;
  border-bottom: 1px solid #f0f0f0;
}

.header-logo {
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: 600;
  color: #667eea;
}

.header-help {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.link-register {
  color: #667eea;
  font-weight: 500;
  text-decoration: none;
}

.link-register:hover {
  text-decoration: underline;
}

/* 登录主区域 */
.login-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px;
}

.login-card {
  max-width: 420px;
  margin: 0 auto;
  width: 100%;
}

.card-header {
  margin-bottom: 40px;
}

.card-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-top: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 14px 16px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #667eea inset;
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-box {
  width: 120px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.captcha-box:hover {
  border-color: #667eea;
  transform: scale(1.02);
}

.captcha-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-hint {
  font-size: 12px;
  color: #999;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.link-forgot {
  color: #667eea;
  font-size: 14px;
  text-decoration: none;
}

.link-forgot:hover {
  text-decoration: underline;
}

/* 登录按钮 */
.form-submit {
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

/* 分割线 */
.login-divider {
  text-align: center;
  position: relative;
  margin: 32px 0 24px;
}

.login-divider::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 1px;
  background: #e5e7eb;
}

.login-divider span {
  position: relative;
  background: #fff;
  padding: 0 16px;
  color: #999;
  font-size: 13px;
}

/* 其他登录方式 */
.other-methods {
  display: flex;
  justify-content: center;
}

.method-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.method-icon {
  font-size: 18px;
}

/* 页脚 */
.login-footer {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .login-banner {
    display: none;
  }
  
  .login-form-section {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .login-main {
    padding: 32px 24px;
  }
  
  .card-header h2 {
    font-size: 28px;
  }
  
  .form-header {
    padding: 20px 24px;
  }
}
</style>