<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getSeckillList,
  createSeckillCoupon,
  updateSeckillCoupon,
  deleteSeckillCoupon,
} from '@/api/seckill'
import { CouponType, CouponStatus, type SeckillCoupon } from '@/types/coupon'

const loading = ref(false)
const coupons = ref<SeckillCoupon[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await getSeckillList({ page: page.value, size })
    coupons.value = res.data.list || []
    total.value = res.data.total
  } catch {
    coupons.value = []
  } finally {
    loading.value = false
  }
}

const discountText = (row: SeckillCoupon) => {
  if (row.discountType === CouponType.FULL_REDUCTION) {
    return `满${(row.minAmount / 100).toFixed(0)}减${(row.discountValue / 100).toFixed(0)}`
  }
  return `${(row.discountValue / 10).toFixed(1)} 折`
}

const statusMap: Record<number, { text: string; type: 'info' | 'success' | 'danger' }> = {
  [CouponStatus.NOT_STARTED]: { text: '未开始', type: 'info' },
  [CouponStatus.ACTIVE]: { text: '进行中', type: 'success' },
  [CouponStatus.ENDED]: { text: '已结束', type: 'danger' },
}

// 新建/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref(0)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  description: '',
  discountType: CouponType.FULL_REDUCTION as number,
  discountValue: undefined as number | undefined,
  minAmount: 0,
  totalCount: undefined as number | undefined,
  perUserLimit: 1,
  timeRange: [] as string[],
})

const isFullReduction = () => form.discountType === CouponType.FULL_REDUCTION

const formRules: FormRules = {
  name: [{ required: true, message: '请输入券名称', trigger: 'blur' }],
  discountType: [{ required: true, message: '请选择券类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择生效时间', trigger: 'change' }],
}

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.discountType = CouponType.FULL_REDUCTION
  form.discountValue = undefined
  form.minAmount = 0
  form.totalCount = undefined
  form.perUserLimit = 1
  form.timeRange = []
  formRef.value?.clearValidate()
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = 0
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: SeckillCoupon) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.name = row.name
  form.description = row.description
  form.discountType = row.discountType
  form.discountValue = row.discountType === CouponType.FULL_REDUCTION
    ? row.discountValue / 100
    : row.discountValue
  form.minAmount = row.minAmount / 100
  form.totalCount = row.totalCount
  form.perUserLimit = row.perUserLimit
  form.timeRange = [row.startTime, row.endTime]
  dialogVisible.value = true
}

const handleDelete = async (row: SeckillCoupon) => {
  try {
    await ElMessageBox.confirm(`确定要删除优惠券「${row.name}」吗？`, '删除优惠券', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteSeckillCoupon(row.id)
    ElMessage.success('删除成功')
    loadCoupons()
  } catch {
    // 错误提示由拦截器统一处理
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const payload = {
    name: form.name,
    description: form.description,
    discountType: form.discountType,
    discountValue: isFullReduction()
      ? Math.round((form.discountValue ?? 0) * 100)
      : form.discountValue ?? 0,
    minAmount: Math.round(form.minAmount * 100),
    totalCount: form.totalCount ?? 0,
    startTime: form.timeRange[0],
    endTime: form.timeRange[1],
    perUserLimit: form.perUserLimit,
  }

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createSeckillCoupon(payload)
      ElMessage.success('创建成功')
    } else {
      await updateSeckillCoupon(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadCoupons()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCoupons()
})
</script>

<template>
  <div class="cm-page">
    <div class="cm-header">
      <h2>秒杀券管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建优惠券</el-button>
    </div>

    <el-table :data="coupons" v-loading="loading" stripe class="cm-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="券名称" min-width="140" />
      <el-table-column label="优惠" width="120" align="center">
        <template #default="{ row }">
          <span class="cm-discount">{{ discountText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存" width="100" align="center">
        <template #default="{ row }">{{ row.remainCount }}/{{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="每人限领" width="90" align="center">
        <template #default="{ row }">{{ row.perUserLimit }} 张</template>
      </el-table-column>
      <el-table-column label="生效时间" min-width="220">
        <template #default="{ row }">
          <div class="cm-time">
            <p>{{ row.startTime }}</p>
            <p>{{ row.endTime }}</p>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button text size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="cm-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadCoupons"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建优惠券' : '编辑优惠券'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="券名称" prop="name">
          <el-input v-model="form.name" placeholder="如：新人满减券" maxlength="30" />
        </el-form-item>
        <el-form-item label="券描述">
          <el-input v-model="form.description" placeholder="选填，简要说明适用范围" maxlength="100" />
        </el-form-item>
        <el-form-item label="券类型" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio :value="CouponType.FULL_REDUCTION">满减券</el-radio>
            <el-radio :value="CouponType.DISCOUNT">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isFullReduction()" label="减免金额" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0.01" :precision="2" :step="1" />
          <span class="cm-unit">元</span>
        </el-form-item>
        <el-form-item v-else label="折扣" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="1" :max="99" :precision="1" :step="0.5" />
          <span class="cm-unit">（如 85 表示 8.5 折）</span>
        </el-form-item>
        <el-form-item label="使用门槛">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="1" />
          <span class="cm-unit">元（0 表示无门槛）</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="每人限领">
          <el-input-number v-model="form.perUserLimit" :min="1" :max="10" :step="1" />
        </el-form-item>
        <el-form-item label="生效时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="cm-range"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.cm-page {
  padding: 20px;
}

.cm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.cm-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.cm-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.cm-discount {
  color: #e2231a;
  font-weight: 600;
}

.cm-time p {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.cm-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.cm-unit {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

.cm-range {
  width: 100%;
}
</style>
