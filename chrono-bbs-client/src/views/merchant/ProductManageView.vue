<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMerchantProducts } from '@/api/merchant'
import {
  getProductDetail,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  getCategoryTree,
} from '@/api/product'
import type { Product, Category, ProductFormData } from '@/types/product'

const loading = ref(false)
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = 10

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getMerchantProducts({ page: page.value, size })
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

// 分类树（用于商品分类级联选择）
const categories = ref<Category[]>([])

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {
    categories.value = []
  }
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref(0)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  brand: '',
  categoryId: undefined as number | undefined,
  description: '',
  price: 0,
  stock: 100,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
}

const resetForm = () => {
  form.name = ''
  form.brand = ''
  form.categoryId = undefined
  form.description = ''
  form.price = 0
  form.stock = 100
  formRef.value?.clearValidate()
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = 0
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (product: Product) => {
  dialogMode.value = 'edit'
  editingId.value = product.id
  resetForm()
  // 拉详情回填完整字段
  try {
    const res = await getProductDetail(product.id)
    const detail = res.data
    form.name = detail.name
    form.brand = detail.brand
    form.categoryId = detail.categoryId
    form.description = detail.description
    const firstSku = detail.skus?.[0]
    form.price = firstSku ? firstSku.price / 100 : 0
    form.stock = firstSku ? firstSku.stock : 100
  } catch {
    form.name = product.name
  }
  dialogVisible.value = true
}

const buildPayload = (): ProductFormData => ({
  name: form.name,
  description: form.description || undefined,
  brand: form.brand || undefined,
  categoryId: form.categoryId as number,
  skuList: [
    {
      specIds: '',
      specText: '默认规格',
      price: Math.round(form.price * 100),
      stock: form.stock,
    },
  ],
})

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createProduct(buildPayload())
      ElMessage.success('商品创建成功')
    } else {
      await updateProduct(editingId.value, buildPayload())
      ElMessage.success('商品更新成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (product: Product) => {
  const newStatus = product.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}商品「${product.name}」吗？`, `${action}商品`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(`已${action}商品`)
    loadProducts()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleRemove = async (product: Product) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品「${product.name}」吗？删除后不可恢复。`, '删除商品', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteProduct(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<template>
  <div class="pm-page">
    <div class="pm-header">
      <h2>商品管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>

    <el-table :data="products" v-loading="loading" stripe class="pm-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-cell">
            <div class="product-img"><span>暂无图片</span></div>
            <p class="p-name">{{ row.name }}</p>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">¥{{ priceYuan(row.minPrice) }}</template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button text size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
          <el-button text size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleRemove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pm-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增商品' : '编辑商品'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="60" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="categories"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            :emit-path="false"
            placeholder="请选择商品分类（三级）"
            clearable
            class="pm-cascader"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" placeholder="选填，如：华为" maxlength="30" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
          <span class="pm-unit">元</span>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填" maxlength="200" />
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
.pm-page {
  padding: 20px;
}

.pm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pm-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.pm-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  border: 1px solid #eee;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #bbb;
  flex-shrink: 0;
}

.p-name {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.pm-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.pm-unit {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

.pm-cascader {
  width: 100%;
}
</style>
