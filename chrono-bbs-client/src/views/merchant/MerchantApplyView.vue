<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyMerchant, getMerchantInfo } from '@/api/merchant'
import type { MerchantInfo } from '@/api/merchant'

const loading = ref(false)
const submitting = ref(false)
const merchantInfo = ref<MerchantInfo | null>(null)

const form = ref({
  shopName: '',
  contactPhone: '',
  businessLicense: '',
})

const loadInfo = async () => {
  loading.value = true
  try {
    const res = await getMerchantInfo()
    merchantInfo.value = res.data
    form.value.shopName = res.data.shopName
    form.value.contactPhone = res.data.contactPhone
  } catch {
    merchantInfo.value = null
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.shopName || !form.value.contactPhone) {
    ElMessage.warning('请填写店铺名称和联系电话')
    return
  }
  submitting.value = true
  try {
    await applyMerchant({
      shopName: form.value.shopName,
      contactPhone: form.value.contactPhone,
    })
    ElMessage.success('入驻申请已提交，请等待管理员审核')
    loadInfo()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const statusLabel: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }

onMounted(() => {
  loadInfo()
})
</script>

<template>
  <div class="ma-page" v-loading="loading">
    <h2 class="ma-title">商家入驻</h2>

    <template v-if="merchantInfo">
      <div class="ma-card">
        <div class="ma-status" :class="`status-${merchantInfo.status}`">
          <span class="status-dot"></span>
          {{ statusLabel[merchantInfo.status] || '未知' }}
        </div>
        <div class="ma-info-grid">
          <div class="info-item">
            <label>店铺名称</label>
            <span>{{ merchantInfo.shopName }}</span>
          </div>
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ merchantInfo.contactPhone }}</span>
          </div>
          <div class="info-item">
            <label>提交时间</label>
            <span>{{ merchantInfo.createdAt }}</span>
          </div>
          <div class="info-item" v-if="merchantInfo.reviewedAt">
            <label>审核时间</label>
            <span>{{ merchantInfo.reviewedAt }}</span>
          </div>
          <div class="info-item" v-if="merchantInfo.reviewReason">
            <label>审核意见</label>
            <span>{{ merchantInfo.reviewReason }}</span>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="ma-card">
        <h3>填写入驻信息</h3>
        <el-form label-width="100px" class="ma-form">
          <el-form-item label="店铺名称" required>
            <el-input v-model="form.shopName" placeholder="请输入店铺名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="11" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              提交入驻申请
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </template>
  </div>
</template>

<style scoped>
.ma-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.ma-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 20px;
}

.ma-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.ma-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.ma-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-0 { color: #e6a23c; }
.status-0 .status-dot { background: #e6a23c; }
.status-1 { color: #67c23a; }
.status-1 .status-dot { background: #67c23a; }
.status-2 { color: #f56c6c; }
.status-2 .status-dot { background: #f56c6c; }

.ma-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}
.info-item span {
  font-size: 15px;
  color: #333;
}

.ma-form {
  margin-top: 8px;
}
</style>