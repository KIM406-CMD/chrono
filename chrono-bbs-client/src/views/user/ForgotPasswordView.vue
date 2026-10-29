<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, sendForgotCode, resetPassword } from '@/api/user'

const router = useRouter()
const step = ref(1)
const loading = ref(false)
const countdown = ref(0)

const captchaImage = ref('')
const captchaUuid = ref('')
const verifyUuid = ref('')
const maskAccount = ref('')
let countdownTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  account: '',
  captchaCode: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const loadCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaUuid.value = res.data.uuid
    captchaImage.value = res.data.image
  } catch {
    ElMessage.error('获取验证码失败')
  }
}

const startCountdown = () => {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!form.account || form.account.length < 5) {
    ElMessage.warning('请输入正确的手机号或邮箱')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入图形验证码')
    return
  }
  loading.value = true
  try {
    const res = await sendForgotCode({
      account: form.account,
      captchaUuid: captchaUuid.value,
      captchaCode: form.captchaCode,
    })
    verifyUuid.value = res.data.uuid
    maskAccount.value = res.data.maskAccount
    startCountdown()
    step.value = 2
    ElMessage.success('验证码已发送')
  } catch {
    loadCaptcha()
    form.captchaCode = ''
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  if (!form.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!form.newPassword || form.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await resetPassword({
      uuid: verifyUuid.value,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    })
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<template>
  <div class="fp-wrapper">
    <div class="fp-card">
      <div class="fp-header">
        <h2>忘记密码</h2>
        <p class="fp-sub">请按步骤重置您的登录密码</p>
      </div>

      <el-steps :active="step - 1" align-center class="fp-steps">
        <el-step title="验证账号" />
        <el-step title="短信验证" />
        <el-step title="设置新密码" />
      </el-steps>

      <!-- Step 1: Account + Captcha -->
      <el-form v-if="step === 1" :model="form" label-width="0" class="fp-form">
        <el-form-item>
          <el-input
            v-model="form.account"
            placeholder="请输入注册时使用的手机号或邮箱"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="图形验证码"
              size="large"
            />
            <img
              :src="captchaImage"
              alt="验证码"
              class="captcha-img"
              title="点击刷新"
              @click="loadCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="handleSendCode">
            获取短信验证码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: Verify Code -->
      <el-form v-if="step === 2" :model="form" label-width="0" class="fp-form">
        <p class="step-hint">验证码已发送至 {{ maskAccount }}</p>
        <el-form-item>
          <el-input v-model="form.code" placeholder="请输入6位验证码" maxlength="6" size="large">
            <template #append>
              <el-button :disabled="countdown > 0" @click="handleSendCode">
                {{ countdown > 0 ? `${countdown}s后重试` : '重新发送' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="full-btn" @click="handleReset">
            下一步
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Step 3: Set Password -->
      <el-form v-if="step === 3" :model="form" label-width="0" class="fp-form">
        <el-form-item>
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（6-20位）"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="handleReset">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="fp-footer">
        <el-button text type="primary" @click="router.push('/login')">返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fp-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.fp-card {
  width: 440px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
}

.fp-header {
  text-align: center;
  margin-bottom: 28px;
}

.fp-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.fp-sub {
  margin: 8px 0 0;
  font-size: 14px;
  color: #999;
}

.fp-steps {
  margin-bottom: 32px;
}

.fp-form {
  margin-top: 16px;
}

.step-hint {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px;
  text-align: center;
}

.full-btn {
  width: 100%;
}

.fp-footer {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-img {
  height: 40px;
  width: 120px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  flex-shrink: 0;
}
</style>