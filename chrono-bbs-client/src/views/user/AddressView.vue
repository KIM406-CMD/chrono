<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Edit, Delete, MapLocation } from '@element-plus/icons-vue'
import { getAddressList, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'
import type { Address, CreateAddressRequest, UpdateAddressRequest } from '@/types/address'

const router = useRouter()
const loading = ref(false)
const addresses = ref<Address[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const submitting = ref(false)

const form = ref<CreateAddressRequest>({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 1,
})

const formRules = {
  receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  form.value = {
    receiverName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 1,
  }
  dialogVisible.value = true
}

const openEdit = (addr: Address) => {
  editingId.value = addr.id
  form.value = {
    receiverName: addr.receiverName,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    isDefault: addr.isDefault,
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingId.value) {
      await updateAddress({ id: editingId.value, ...form.value } as UpdateAddressRequest)
      ElMessage.success('修改成功')
    } else {
      await createAddress(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (addr: Address) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '删除地址', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteAddress(addr.id)
    ElMessage.success('已删除')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleSetDefault = async (addr: Address) => {
  if (addr.isDefault === 1) return
  try {
    await setDefaultAddress(addr.id)
    ElMessage.success('已设为默认地址')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="address-page">
    <div class="ad-header">
      <el-button text class="back-btn" @click="router.push('/home')">
        <ArrowLeft />
        <span>返回首页</span>
      </el-button>
      <h2 class="ad-title">收货地址管理</h2>
    </div>

    <div v-loading="loading" class="ad-body">
      <div v-if="addresses.length === 0 && !loading" class="ad-empty">
        <div class="empty-icon">📍</div>
        <p>暂无收货地址</p>
        <el-button type="primary" @click="openCreate">添加新地址</el-button>
      </div>

      <div v-else class="ad-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="ad-card"
          :class="{ default: addr.isDefault === 1 }"
        >
          <div class="ad-card-head">
            <strong>{{ addr.receiverName }}</strong>
            <span class="ad-phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="danger" class="ad-tag">默认</el-tag>
          </div>
          <div class="ad-card-body">
            <MapLocation class="ad-loc-icon" />
            {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
          </div>
          <div class="ad-card-footer">
            <el-button
              v-if="addr.isDefault !== 1"
              text
              size="small"
              @click="handleSetDefault(addr)"
            >
              设为默认
            </el-button>
            <span v-else class="default-text">默认地址</span>
            <el-button text size="small" :icon="Edit" @click="openEdit(addr)">编辑</el-button>
            <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(addr)">
              删除
            </el-button>
          </div>
        </div>

        <div class="ad-add-card" @click="openCreate">
          <Plus />
          <span>新增收货地址</span>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑地址' : '新增地址'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="收件人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <div class="region-row">
            <el-form-item prop="province" class="region-item">
              <el-input v-model="form.province" placeholder="省" />
            </el-form-item>
            <el-form-item prop="city" class="region-item">
              <el-input v-model="form.city" placeholder="市" />
            </el-form-item>
            <el-form-item prop="district" class="region-item">
              <el-input v-model="form.district" placeholder="区/县" />
            </el-form-item>
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch
            :model-value="form.isDefault === 1"
            @update:model-value="(v: boolean) => form.isDefault = v ? 1 : 0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.address-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.ad-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn { font-size: 14px; color: #666; }
.back-btn:hover { color: #e2231a; }

.ad-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

/* ========== 空 ========== */
.ad-empty {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
}

.empty-icon { font-size: 64px; margin-bottom: 12px; }
.ad-empty p { font-size: 16px; color: #999; margin: 0 0 16px; }

/* ========== 列表 ========== */
.ad-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.ad-card {
  width: calc(50% - 8px);
  background: #fff;
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 18px;
  transition: border-color 0.2s;
  position: relative;
}

.ad-card.default {
  border-color: #f5c6c6;
}

.ad-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 15px;
}

.ad-phone {
  font-size: 13px;
  color: #999;
}

.ad-tag { transform: scale(0.85); }

.ad-card-body {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.ad-loc-icon {
  color: #e2231a;
  margin-top: 2px;
  flex-shrink: 0;
}

.ad-card-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.default-text {
  font-size: 12px;
  color: #e2231a;
  margin-right: auto;
}

.ad-add-card {
  width: calc(50% - 8px);
  min-height: 140px;
  background: #fff;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  color: #999;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.ad-add-card:hover {
  border-color: #e2231a;
  color: #e2231a;
}

/* ========== 弹窗 ========== */
.region-row {
  display: flex;
  gap: 8px;
}
.region-item {
  flex: 1;
  margin-bottom: 0;
}
</style>