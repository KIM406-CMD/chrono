<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadUserFile } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMerchantProducts } from '@/api/merchant'
import { uploadImage } from '@/api/upload'
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

// 商家列表的 images 为 JSON 数组字符串，兼容数组形式
const firstImage = (images?: string | string[]) => {
  if (!images) return ''
  let list: unknown = images
  if (typeof images === 'string') {
    try {
      list = JSON.parse(images)
    } catch {
      return ''
    }
  }
  return Array.isArray(list) && list.length > 0 ? String(list[0]) : ''
}

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
})

interface SpecGroup {
  /** 规格名，如“颜色” */
  name: string
  /** 逗号分隔的规格值，如“红色,蓝色” */
  values: string
}

interface SkuRow {
  specIds: string
  specText: string
  /** 售价，单位：元（提交时转分） */
  price: number
  stock: number
}

const defaultSpecGroups = (): SpecGroup[] => [{ name: '规格', values: '默认规格' }]
const defaultSkuRows = (): SkuRow[] => [
  { specIds: '', specText: '默认规格', price: 0, stock: 100 },
]

const specGroups = ref<SpecGroup[]>(defaultSpecGroups())
const skuRows = ref<SkuRow[]>(defaultSkuRows())

const imageUrls = ref<string[]>([])
const imageFileList = ref<UploadUserFile[]>([])

const formRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
}

const resetForm = () => {
  form.name = ''
  form.brand = ''
  form.categoryId = undefined
  form.description = ''
  specGroups.value = defaultSpecGroups()
  skuRows.value = defaultSkuRows()
  imageUrls.value = []
  imageFileList.value = []
  formRef.value?.clearValidate()
}

const parseSpecValues = (raw: string) =>
  Array.from(new Set(raw.split(/[,，]/).map((value) => value.trim()).filter(Boolean)))

// 根据规格组生成笛卡尔积 SKU，保留同规格组合已填写的价格/库存
const regenerateSkus = () => {
  const groups = specGroups.value
    .map((group) => ({
      name: group.name.trim() || '规格',
      values: parseSpecValues(group.values),
    }))
    .filter((group) => group.values.length > 0)

  if (!groups.length) {
    skuRows.value = []
    ElMessage.warning('请至少填写一个规格值')
    return
  }

  const total = groups.reduce((count, group) => count * group.values.length, 1)
  if (total > 100) {
    ElMessage.warning('规格组合超过 100 个，请精简规格值')
    return
  }

  let combinations: { values: string[]; ids: number[] }[] = [{ values: [], ids: [] }]
  let idBase = 0
  for (const group of groups) {
    const next: typeof combinations = []
    for (const combo of combinations) {
      group.values.forEach((value, index) => {
        next.push({
          values: [...combo.values, value],
          ids: [...combo.ids, idBase + index + 1],
        })
      })
    }
    idBase += group.values.length
    combinations = next
  }

  const existing = new Map(skuRows.value.map((row) => [row.specText, row]))
  skuRows.value = combinations.map((combo) => {
    const specText = combo.values.join('/')
    const previous = existing.get(specText)
    return {
      specIds: JSON.stringify(combo.ids),
      specText,
      price: previous?.price ?? 0,
      stock: previous?.stock ?? 0,
    }
  })
}

const addSpecGroup = () => {
  specGroups.value.push({ name: `规格${specGroups.value.length + 1}`, values: '' })
}

const removeSpecGroup = (index: number) => {
  specGroups.value.splice(index, 1)
  if (!specGroups.value.length) {
    specGroups.value = defaultSpecGroups()
  }
  regenerateSkus()
}

// 由已有 SKU 的 specText 反推规格组（编辑回填）
const buildSpecGroups = (skus: { specText?: string }[]): SpecGroup[] => {
  const segments = skus.map((sku) => (sku.specText || '默认规格').split('/'))
  const depth = Math.max(...segments.map((item) => item.length), 1)
  return Array.from({ length: depth }, (_, index) => ({
    name: depth === 1 ? '规格' : `规格${index + 1}`,
    values: Array.from(new Set(segments.map((item) => item[index]).filter(Boolean))).join(','),
  }))
}

const syncImageUrls = () => {
  imageUrls.value = imageFileList.value.map((file) => file.url).filter((url): url is string => !!url)
}

const handleImageChange = async (file: UploadUserFile) => {
  if (!file.raw) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)) {
    ElMessage.warning('仅支持 jpg/jpeg/png/webp 格式')
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  try {
    const res = await uploadImage(file.raw)
    file.url = res.data.url
    syncImageUrls()
  } catch {
    imageFileList.value = imageFileList.value.filter((item) => item.uid !== file.uid)
  }
}

const handleImageRemove = () => {
  syncImageUrls()
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
    const skus = detail.skuList || []
    specGroups.value = skus.length ? buildSpecGroups(skus) : defaultSpecGroups()
    skuRows.value = skus.length
      ? skus.map((sku) => ({
          specIds: sku.specIds || '',
          specText: sku.specText || '默认规格',
          price: sku.price / 100,
          stock: sku.stock,
        }))
      : defaultSkuRows()
    imageUrls.value = [...(detail.images || [])]
    imageFileList.value = imageUrls.value.map((url) => ({ name: '商品图', url }))
  } catch {
    form.name = product.name
    imageUrls.value = []
    imageFileList.value = []
  }
  dialogVisible.value = true
}

const buildPayload = (): ProductFormData => ({
  name: form.name,
  description: form.description || undefined,
  images: imageUrls.value.length ? [...imageUrls.value] : undefined,
  brand: form.brand || undefined,
  categoryId: form.categoryId as number,
  skuList: skuRows.value.map((sku) => ({
    specIds: sku.specIds,
    specText: sku.specText,
    price: Math.round(sku.price * 100),
    stock: sku.stock,
  })),
})

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!skuRows.value.length) {
    ElMessage.warning('请至少配置一个 SKU')
    return
  }
  if (skuRows.value.some((sku) => sku.price <= 0)) {
    ElMessage.warning('每个 SKU 的价格必须大于 0')
    return
  }
  if (skuRows.value.some((sku) => sku.stock < 0)) {
    ElMessage.warning('SKU 库存不能为负数')
    return
  }
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
            <el-image
              v-if="firstImage(row.images)"
              :src="firstImage(row.images)"
              fit="cover"
              class="product-img"
            />
            <div v-else class="product-img"><span>暂无图片</span></div>
            <p class="p-name">{{ row.name }}</p>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">{{ row.minPrice != null ? `¥${priceYuan(row.minPrice)}` : '—' }}</template>
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
      width="760px"
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
        <el-form-item label="规格">
          <div class="spec-editor">
            <div v-for="(group, index) in specGroups" :key="index" class="spec-row">
              <el-input v-model="group.name" placeholder="规格名，如颜色" class="spec-name" />
              <el-input v-model="group.values" placeholder="规格值，用逗号分隔，如 红色,蓝色" class="spec-values" />
              <el-button text type="danger" @click="removeSpecGroup(index)">删除</el-button>
            </div>
            <div class="spec-actions">
              <el-button text type="primary" @click="addSpecGroup">+ 添加规格</el-button>
              <el-button text @click="regenerateSkus">生成 / 刷新 SKU</el-button>
              <span class="spec-hint">修改规格值后需重新生成</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="SKU">
          <el-table :data="skuRows" size="small" border class="sku-table" empty-text="请先配置规格并生成 SKU">
            <el-table-column prop="specText" label="规格组合" min-width="140" />
            <el-table-column label="价格（元）" width="170">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.price"
                  :min="0"
                  :precision="2"
                  :step="1"
                  size="small"
                  controls-position="right"
                />
              </template>
            </el-table-column>
            <el-table-column label="库存" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.stock"
                  :min="0"
                  :step="1"
                  size="small"
                  controls-position="right"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填" maxlength="200" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            v-model:file-list="imageFileList"
            list-type="picture-card"
            :auto-upload="false"
            :limit="4"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <span class="pm-unit">最多 4 张，单张 ≤2MB</span>
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

.spec-editor {
  width: 100%;
}

.spec-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.spec-name {
  width: 180px;
  flex-shrink: 0;
}

.spec-values {
  flex: 1;
}

.spec-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spec-hint {
  font-size: 12px;
  color: #999;
}

.sku-table {
  width: 100%;
}
</style>
