<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key, Phone } from '@element-plus/icons-vue'
import { register, getCaptcha } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { RegisterRequest } from '@/types/user'
import { Role } from '@/types/common'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)
const captchaUrl = ref('')
const captchaUuid = ref('')
const agreeDialogVisible = ref(false)
const agreeDialogType = ref<'terms' | 'privacy'>('terms')

const phoneDialogVisible = ref(false)
const phoneLoading = ref(false)
const phoneForm = reactive({
  phone: '',
  smsCode: '',
})
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const agreementContents = {
  terms: {
    title: '用户服务协议',
    paragraphs: [
      '一、协议的接受',
      '您在注册成为本商城用户前，应当认真阅读本协议全部内容。您勾选"我已阅读并同意"并完成注册，即表示您已充分理解并同意本协议的全部条款。',
      '二、账号注册与管理',
      '1. 您应提供真实、准确、完整的注册信息，并及时更新。\n2. 账号仅限本人使用，不得出借、转让或出售。\n3. 您应妥善保管账号与密码，因保管不当造成的损失由您自行承担。',
      '三、用户行为规范',
      '您承诺不利用本平台从事违法违规活动，包括但不限于：发布虚假信息、恶意刷单、侵犯他人知识产权、干扰平台正常运营等。如发现违规行为，平台有权限制或终止您的账号使用。',
      '四、交易规则',
      '商品信息由入驻商家发布，平台对商家资质进行审核。下单即视为您与商家达成买卖合同，具体权利义务以订单页面展示为准。',
      '五、协议的变更与终止',
      '平台有权根据业务需要修订本协议，修订内容将在平台公示。若您不同意修订内容，可停止使用本服务；继续使用则视为接受修订后的协议。',
    ],
  },
  privacy: {
    title: '隐私政策',
    paragraphs: [
      '一、我们收集的信息',
      '1. 账号信息：用户名、密码、手机号、邮箱等注册信息。\n2. 交易信息：订单记录、收货地址、支付方式等。\n3. 使用信息：浏览记录、搜索关键词、设备信息等。',
      '二、信息的使用',
      '我们仅在以下场景使用您的信息：完成注册与登录验证、处理订单与物流配送、提供售后服务、改进产品与服务质量、在您同意的前提下推送营销信息。',
      '三、信息的共享',
      '除以下情形外，我们不会向第三方共享您的个人信息：\n1. 为完成订单配送，向物流服务商提供必要的收货信息；\n2. 为完成支付，向支付机构提供必要的交易信息；\n3. 依据法律法规或监管要求必须提供的情形。',
      '四、信息的保护',
      '我们采用加密传输、访问控制等技术手段保护您的信息安全，防止信息被未授权访问、泄露或篡改。',
      '五、您的权利',
      '您有权查询、更正、删除您的个人信息，也可在"账户设置"中管理相关偏好。如需注销账号，可通过帮助中心联系我们。',
    ],
  },
} as const

const currentAgreement = computed(() => agreementContents[agreeDialogType.value])

const openAgreement = (type: 'terms' | 'privacy') => {
  agreeDialogType.value = type
  agreeDialogVisible.value = true
}

const registerForm = reactive<RegisterRequest>({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  role: Role.USER,
  captchaUuid: '',
  captchaCode: '',
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
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
    registerForm.captchaUuid = res.data.uuid
  } catch (error) {
    captchaUrl.value = ''
  }
}

// 注册
const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    registerForm.captchaUuid = captchaUuid.value
    const res = await register(registerForm)
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
    ElMessage.success('注册成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
    fetchCaptcha()
    registerForm.captchaCode = ''
  } finally {
    loading.value = false
  }
}

// 手机号快捷注册
const openPhoneRegister = () => {
  phoneForm.phone = ''
  phoneForm.smsCode = ''
  phoneDialogVisible.value = true
}

const sendRegisterSmsCode = () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0 && smsTimer) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
  ElMessage.success('验证码已发送（开发环境任意 6 位数字均可）')
}

const handlePhoneRegister = async () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!/^\d{6}$/.test(phoneForm.smsCode)) {
    ElMessage.warning('请输入 6 位短信验证码')
    return
  }

  phoneLoading.value = true
  try {
    const res = await register({
      username: phoneForm.phone,
      password: phoneForm.smsCode,
      confirmPassword: phoneForm.smsCode,
      phone: phoneForm.phone,
      role: Role.USER,
      captchaUuid: '',
      captchaCode: '',
    })
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
    ElMessage.success('注册成功')
    phoneDialogVisible.value = false
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    phoneLoading.value = false
  }
}

// 回车注册
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleRegister()
  }
}

onMounted(() => {
  fetchCaptcha()
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  if (smsTimer) {
    clearInterval(smsTimer)
    smsTimer = null
  }
})
</script>

<template>
  <div class="register-page">
    <!-- 左侧品牌展示区 -->
    <div class="register-banner">
      <div class="banner-content">
        <div class="brand-logo">
          <div class="logo-icon">🛒</div>
          <h1 class="brand-name">网上商城</h1>
          <p class="brand-slogan">品质生活，从这里开始</p>
        </div>
        
        <div class="banner-benefits">
          <div class="benefit-item">
            <div class="benefit-icon">🎁</div>
            <div class="benefit-text">
              <h3>新人礼包</h3>
              <p>注册即享专属优惠</p>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">⭐</div>
            <div class="benefit-text">
              <h3>会员特权</h3>
              <p>积分兑换，尊享服务</p>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">🔒</div>
            <div class="benefit-text">
              <h3>安全保障</h3>
              <p>信息安全，隐私保护</p>
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

    <!-- 右侧注册表单区 -->
    <div class="register-form-section">
      <!-- 顶部导航 -->
      <header class="form-header">
        <router-link to="/" class="header-logo">
          <span class="logo-text">网上商城</span>
        </router-link>
        <div class="header-help">
          <span>已有账号？</span>
          <router-link to="/login" class="link-login">立即登录</router-link>
        </div>
      </header>

      <!-- 注册卡片 -->
      <main class="register-main">
        <div class="register-card">
          <div class="card-header">
            <h2>创建账号</h2>
            <p class="card-subtitle">填写以下信息完成注册</p>
          </div>

          <el-form
            ref="formRef"
            :model="registerForm"
            :rules="rules"
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（3-20位）"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-20位）"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="captcha-row">
                <el-input
                  v-model="registerForm.captchaCode"
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

            <div class="form-agreement">
              <el-checkbox v-model="agreeTerms">
                我已阅读并同意
              </el-checkbox>
              <span class="agreement-link" @click.stop="openAgreement('terms')">《用户服务协议》</span>
              和
              <span class="agreement-link" @click.stop="openAgreement('privacy')">《隐私政策》</span>
            </div>

            <el-form-item class="form-submit">
              <el-button
                type="primary"
                size="large"
                class="register-btn"
                :loading="loading"
                :disabled="!agreeTerms"
                @click="handleRegister"
              >
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-divider">
            <span>其他注册方式</span>
          </div>

          <div class="other-methods">
            <el-button text class="method-btn" @click="openPhoneRegister">
              <span class="method-icon">📱</span>
              <span>手机号快捷注册</span>
            </el-button>
          </div>
        </div>

        <div class="register-footer">
          <p>© 2025 网上商城系统 - 课程设计项目</p>
        </div>
      </main>
    </div>

    <el-dialog
      v-model="agreeDialogVisible"
      :title="currentAgreement.title"
      width="600px"
      class="agreement-dialog"
    >
      <div class="agreement-body">
        <template v-for="(paragraph, index) in currentAgreement.paragraphs" :key="index">
          <h4 v-if="index % 2 === 0" class="agreement-paragraph is-heading">{{ paragraph }}</h4>
          <p v-else class="agreement-paragraph">{{ paragraph }}</p>
        </template>
      </div>
      <template #footer>
        <el-button type="primary" @click="agreeDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="phoneDialogVisible" title="手机号快捷注册" width="420px">
      <el-form label-position="top" class="phone-form">
        <el-form-item label="手机号">
          <el-input v-model="phoneForm.phone" placeholder="请输入手机号" maxlength="11" size="large" />
        </el-form-item>
        <el-form-item label="短信验证码">
          <div class="sms-row">
            <el-input v-model="phoneForm.smsCode" placeholder="请输入 6 位验证码" maxlength="6" size="large" />
            <el-button
              type="primary"
              plain
              :disabled="smsCountdown > 0"
              @click="sendRegisterSmsCode"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="phoneLoading" @click="handlePhoneRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
  background: #fff;
}

/* 左侧品牌展示区 */
.register-banner {
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

.banner-benefits {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.benefit-item {
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

.benefit-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(10px);
}

.benefit-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.benefit-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 6px 0;
}

.benefit-text p {
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

/* 右侧注册表单区 */
.register-form-section {
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

.link-login {
  color: #667eea;
  font-weight: 500;
  text-decoration: none;
}

.link-login:hover {
  text-decoration: underline;
}

/* 注册主区域 */
.register-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px;
}

.register-card {
  max-width: 420px;
  margin: 0 auto;
  width: 100%;
}

.card-header {
  margin-bottom: 32px;
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
.register-form {
  margin-top: 24px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  padding: 14px 16px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.3s;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
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

/* 协议勾选 */
.form-agreement {
  margin-bottom: 20px;
}

.form-agreement :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #666;
}

.agreement-link {
  color: #667eea;
  cursor: pointer;
  font-size: 13px;
}

.agreement-link:hover {
  text-decoration: underline;
}

.agreement-body {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 4px;
}

.agreement-paragraph {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
}

.agreement-paragraph.is-heading {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-top: 16px;
}

.agreement-paragraph.is-heading:first-child {
  margin-top: 0;
}

/* 注册按钮 */
.form-submit {
  margin-bottom: 0;
}

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 分割线 */
.register-divider {
  text-align: center;
  position: relative;
  margin: 28px 0 20px;
}

.register-divider::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 1px;
  background: #e5e7eb;
}

.register-divider span {
  position: relative;
  background: #fff;
  padding: 0 16px;
  color: #999;
  font-size: 13px;
}

/* 其他注册方式 */
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
.register-footer {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .register-banner {
    display: none;
  }
  
  .register-form-section {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .register-main {
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