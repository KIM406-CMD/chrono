<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Bell, Lock, Setting, User } from '@element-plus/icons-vue'
import { getUserInfo, updatePassword, updateProfile } from '@/api/user'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'
import { Role } from '@/types/common'
import type { UserInfo } from '@/types/user'
import { defaultGeneralSettings, generalSettings, saveGeneralSettings } from '@/utils/settings'

const router = useRouter()
const userStore = useUserStore()

const activeSection = ref<'profile' | 'security' | 'general'>('profile')

const generalForm = reactive({ ...generalSettings })

const handleSaveGeneral = () => {
  Object.assign(generalSettings, generalForm)
  saveGeneralSettings()
  ElMessage.success('通用设置已保存')
}

const handleResetGeneral = () => {
  Object.assign(generalForm, defaultGeneralSettings)
  ElMessage.success('已恢复默认，请点击保存生效')
}

const userInfo = ref<UserInfo | null>(userStore.userInfo)
const roleText = computed(() => {
  switch (userStore.role) {
    case Role.USER:
      return '买家'
    case Role.MERCHANT:
      return '商家'
    case Role.ADMIN:
      return '管理员'
    default:
      return ''
  }
})

const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  nickname: '',
  email: '',
})

const profileRules: FormRules = {
  nickname: [{ max: 20, message: '昵称不超过 20 个字符', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
}

const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const profileSaving = ref(false)
const passwordSaving = ref(false)
const avatarPreview = ref('')
const avatarUploading = ref(false)

const avatarInputRef = ref<HTMLInputElement>()

const handleAvatarClick = () => {
  if (avatarInputRef.value) {
    avatarInputRef.value.value = ''
  }
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG 格式')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadImage(file)
    avatarPreview.value = res.data.url
    ElMessage.success('头像上传成功，请点击保存修改')
  } catch {
    readImageAsDataUrl(file)
  } finally {
    avatarUploading.value = false
  }
}

const readImageAsDataUrl = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string
    ElMessage.info('上传服务不可用，已使用本地预览')
  }
  reader.onerror = () => {
    ElMessage.error('图片读取失败')
  }
  reader.readAsDataURL(file)
}

const fillForm = (info: UserInfo | null) => {
  profileForm.nickname = info?.nickname || ''
  profileForm.email = info?.email || ''
  avatarPreview.value = info?.avatar || ''
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    userStore.setUserInfo(res.data)
    fillForm(res.data)
  } catch {
    fillForm(userInfo.value)
  }
}

const handleSaveProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return
  profileSaving.value = true
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      email: profileForm.email,
      avatar: avatarPreview.value,
    })
    if (userInfo.value) {
      userStore.setUserInfo({
        ...userInfo.value,
        nickname: profileForm.nickname,
        email: profileForm.email,
        avatar: avatarPreview.value,
      })
      userInfo.value = userStore.userInfo
    }
    ElMessage.success('个人信息已保存')
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    profileSaving.value = false
  }
}

const handleSavePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  passwordSaving.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordFormRef.value?.resetFields()
    userStore.logout()
    router.push('/login')
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="st-page">
    <h2 class="st-title">账户设置</h2>

    <div class="st-body">
      <aside class="st-sidebar">
        <div class="st-user">
          <div
            class="st-avatar-wrap"
            :class="{ uploading: avatarUploading }"
            title="点击上传头像"
            @click="handleAvatarClick"
          >
            <el-avatar :size="56" :src="avatarPreview || userInfo?.avatar">
              <el-icon :size="28"><User /></el-icon>
            </el-avatar>
            <span class="st-avatar-mask">更换</span>
          </div>
          <p class="st-username">{{ userInfo?.nickname || userInfo?.username || '我的账户' }}</p>
          <el-tag size="small" type="warning" effect="plain">{{ roleText }}</el-tag>
        </div>
        <ul class="st-menu">
          <li
            class="st-menu-item"
            :class="{ active: activeSection === 'profile' }"
            @click="activeSection = 'profile'"
          >
            <el-icon><User /></el-icon>
            个人资料
          </li>
          <li
            class="st-menu-item"
            :class="{ active: activeSection === 'security' }"
            @click="activeSection = 'security'"
          >
            <el-icon><Lock /></el-icon>
            账户安全
          </li>
          <li
            class="st-menu-item"
            :class="{ active: activeSection === 'general' }"
            @click="activeSection = 'general'"
          >
            <el-icon><Setting /></el-icon>
            通用设置
          </li>
        </ul>
      </aside>

      <section class="st-panel">
        <template v-if="activeSection === 'profile'">
          <h3 class="st-panel-title">个人资料</h3>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="84px"
            class="st-form"
          >
            <el-form-item label="头像">
              <div class="st-avatar-row">
                <el-avatar :size="72" :src="avatarPreview">
                  <el-icon :size="32"><User /></el-icon>
                </el-avatar>
                <div class="st-avatar-actions">
                  <el-button size="small" :loading="avatarUploading" @click="handleAvatarClick">
                    选择图片
                  </el-button>
                  <p class="st-avatar-hint">支持 JPG/PNG，不超过 2MB</p>
                </div>
              </div>
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/jpeg,image/png"
                class="st-avatar-input"
                @change="handleAvatarChange"
              />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input :model-value="userInfo?.username || ''" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input :model-value="userInfo?.phone || ''" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">
                保存修改
              </el-button>
              <el-button @click="fillForm(userInfo)">重置</el-button>
            </el-form-item>
          </el-form>
        </template>

        <template v-else-if="activeSection === 'security'">
          <h3 class="st-panel-title">修改密码</h3>
          <el-alert
            title="修改密码成功后需要重新登录"
            type="info"
            :closable="false"
            show-icon
            class="st-tip"
          />
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="st-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="6-20 位新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordSaving" @click="handleSavePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </template>

        <template v-else>
          <h3 class="st-panel-title">通用设置</h3>
          <el-alert
            title="以下为浏览器本地偏好设置，仅对当前设备生效"
            type="info"
            :closable="false"
            show-icon
            class="st-tip"
          />

          <div class="st-group">
            <div class="st-group-title">
              <el-icon><Bell /></el-icon>
              消息通知
            </div>
            <div class="st-row">
              <div class="st-row-text">
                <p class="st-row-label">订单状态通知</p>
                <p class="st-row-desc">订单支付、发货、签收等状态变化时提醒我</p>
              </div>
              <el-switch v-model="generalForm.orderNotice" />
            </div>
            <div class="st-row">
              <div class="st-row-text">
                <p class="st-row-label">促销活动通知</p>
                <p class="st-row-desc">限时特惠、优惠券等营销信息推送</p>
              </div>
              <el-switch v-model="generalForm.promotionNotice" />
            </div>
            <div class="st-row">
              <div class="st-row-text">
                <p class="st-row-label">聊天消息提醒</p>
                <p class="st-row-desc">收到商家或其他用户的新消息时提醒</p>
              </div>
              <el-switch v-model="generalForm.chatNotice" />
            </div>
          </div>

          <div class="st-group">
            <div class="st-group-title">
              <el-icon><Setting /></el-icon>
              浏览偏好
            </div>
            <div class="st-row">
              <div class="st-row-text">
                <p class="st-row-label">紧凑列表模式</p>
                <p class="st-row-desc">商品列表减少留白，单屏展示更多商品</p>
              </div>
              <el-switch v-model="generalForm.compactList" />
            </div>
            <div class="st-row">
              <div class="st-row-text">
                <p class="st-row-label">显示价格小数</p>
                <p class="st-row-desc">关闭后价格仅显示整数部分</p>
              </div>
              <el-switch v-model="generalForm.showPriceDecimal" />
            </div>
          </div>

          <div class="st-actions">
            <el-button @click="handleResetGeneral">恢复默认</el-button>
            <el-button type="primary" @click="handleSaveGeneral">保存设置</el-button>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<style scoped>
.st-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.st-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 20px;
  color: #333;
}

.st-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.st-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 20px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.st-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 0 16px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.st-username {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  word-break: break-all;
  text-align: center;
}

.st-avatar-wrap {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  line-height: 0;
}

.st-avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s;
}

.st-avatar-wrap:hover .st-avatar-mask {
  opacity: 1;
}

.st-avatar-wrap.uploading .st-avatar-mask {
  opacity: 1;
}

.st-avatar-input {
  display: none;
}

.st-avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.st-avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.st-avatar-hint {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.st-menu {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
}

.st-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 20px;
  font-size: 14px;
  color: #555;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}

.st-menu-item:hover {
  color: #e2231a;
  background: #fafafa;
}

.st-menu-item.active {
  color: #e2231a;
  background: #fff5f5;
  border-left-color: #e2231a;
  font-weight: 600;
}

.st-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  min-height: 420px;
}

.st-panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.st-tip {
  margin: 16px 0 8px;
}

.st-form {
  max-width: 420px;
  padding-top: 16px;
}

.st-group {
  padding-top: 8px;
}

.st-group + .st-group {
  margin-top: 16px;
  border-top: 1px dashed #f0f0f0;
}

.st-group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.st-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
}

.st-row + .st-row {
  border-top: 1px solid #f7f7f7;
}

.st-row-text {
  min-width: 0;
}

.st-row-label {
  margin: 0 0 4px;
  font-size: 14px;
  color: #333;
}

.st-row-desc {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.st-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}
</style>