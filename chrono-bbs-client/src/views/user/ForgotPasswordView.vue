<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const step = ref(1) // 1=enter phone, 2=verify code, 3=set password
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const sendCode = () => {
  if (!form.phone || form.phone.length < 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  loading.value = true
  // Mock send code
  setTimeout(() => {
    loading.value = false
    countdown.value = 60
    step.value = 2
    ElMessage.success('验证码已发送')
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  }, 500)
}

const verifyCode = () => {
  if (!form.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  step.value = 3
  ElMessage.success('验证成功')
}

const resetPassword = () => {
  if (!form.newPassword || form.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  }, 800)
}
</script>

<template>
  <div class="fp-wrapper">
    <div class="fp-card">
      <div class="fp-header">
        <h2>忘记密码</h2>
        <p class="fp-sub">请按步骤重置您的登录密码</p>
      </div>

      <el-steps :active="step - 1" align-center class="fp-steps">
        <el-step title="验证手机号" />
        <el-step title="输入验证码" />
        <el-step title="设置新密码" />
      </el-steps>

      <!-- Step 1: Phone -->
      <el-form v-if="step === 1" :model="form" label-width="0" class="fp-form">
        <el-form-item>
          <el-input
            v-model="form.phone"
            placeholder="请输入注册时使用的手机号"
            maxlength="11"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="sendCode">
            获取验证码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: Verify Code -->
      <el-form v-if="step === 2" :model="form" label-width="0" class="fp-form">
        <p class="step-hint">验证码已发送至 {{ form.phone }}</p>
        <el-form-item>
          <el-input v-model="form.code" placeholder="请输入6位验证码" maxlength="6" size="large">
            <template #append>
              <el-button :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s后重试` : '重新发送' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="full-btn" @click="verifyCode">
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
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="resetPassword">
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
</style>