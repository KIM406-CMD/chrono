<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminProducts,
  setAdminProductStatus,
  deleteAdminProduct,
  updateAdminProduct,
} from '@/api/admin'
import { getProductDetail, getCategoryTree } from '@/api/product'

const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const statusFilter = ref<number | ''>('')

const priceYuan = (fen: number) => (fen / 100).toFixed(2)

const loadProducts = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; keyword?: string; status?: number } = {
      page: page.value,
      size,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminProducts(params)
    products.value = res.data.list || []
    total.value = res.data.total
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadProducts()
}

const handleReset = () => {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  loadProducts()
}

// ==================== 编辑商品 ====================
const editVisible = ref(false)
const editSaving = ref(false)
const editLoading = ref(false)
const categories = ref<any[]>([])
const editImages = ref<string[]>([])
const editForm = reactive({
  id: 0,
  name: '',
  brand: '',
  categoryId: undefined as number | undefined,
  description: '',
})
const editSkus = ref<{ specIds: string; specText: string; price: number; stock: number }[]>([])

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {
    categories.value = []
  }
}

const openEdit = async (row: any) => {
  editLoading.value = true
  editVisible.value = true
  try {
    const res = await getProductDetail(row.id)
    const detail = res.data
    editForm.id = detail.id
    editForm.name = detail.name
    editForm.brand = detail.brand
    editForm.categoryId = detail.categoryId
    editForm.description = detail.description
    editImages.value = [...(detail.images || [])]
    editSkus.value = (detail.skuList || []).map((sku) => ({
      specIds: sku.specIds || '',
      specText: sku.specText || '默认规格',
      price: sku.price / 100,
      stock: sku.stock,
    }))
  } catch {
    ElMessage.error('加载商品详情失败')
    editVisible.value = false
  } finally {
    editLoading.value = false
  }
}

const handleEditSave = async () => {
  if (!editForm.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!editForm.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  if (!editSkus.value.length) {
    ElMessage.warning('请至少保留一个 SKU')
    return
  }
  if (editSkus.value.some((sku) => sku.price <= 0)) {
    ElMessage.warning('SKU 价格必须大于 0')
    return
  }
  editSaving.value = true
  try {
    await updateAdminProduct(editForm.id, {
      name: editForm.name,
      description: editForm.description || undefined,
      images: editImages.value.length ? [...editImages.value] : undefined,
      brand: editForm.brand || undefined,
      categoryId: editForm.categoryId,
      skuList: editSkus.value.map((sku) => ({
        specIds: sku.specIds,
        specText: sku.specText,
        price: Math.round(sku.price * 100),
        stock: sku.stock,
      })),
    })
    ElMessage.success('商品更新成功')
    editVisible.value = false
    loadProducts()
  } catch {
    ElMessage.error('更新失败')
  } finally {
    editSaving.value = false
  }
}

const handleToggleStatus = async (row: any) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}商品「${row.name}」吗？`, `${action}商品`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await setAdminProductStatus(row.id, nextStatus)
    ElMessage.success(`已${action}商品`)
    loadProducts()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品「${row.name}」吗？删除后不可恢复。`, '删除商品', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteAdminProduct(row.id)
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
  <div class="pa-page">
    <h2>全平台商品管理</h2>

    <div class="pa-filter">
      <el-input
        v-model="keyword"
        placeholder="商品名称 / 品牌"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="products" v-loading="loading" stripe class="pa-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="所属店铺" width="120">
        <template #default="{ row }">{{ row.merchantName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="80" />
      <el-table-column label="价格" width="100" align="right">
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
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pa-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
      />
    </div>

    <el-dialog v-model="editVisible" title="编辑商品" width="720px" destroy-on-close>
      <div v-loading="editLoading">
        <el-form label-width="80px">
          <el-form-item label="商品名称">
            <el-input v-model="editForm.name" maxlength="60" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品分类">
            <el-cascader
              v-model="editForm.categoryId"
              :options="categories"
              :props="{ value: 'id', label: 'name', children: 'children' }"
              :emit-path="false"
              placeholder="请选择商品分类（三级）"
              clearable
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="品牌">
            <el-input v-model="editForm.brand" maxlength="30" placeholder="选填" />
          </el-form-item>
          <el-form-item label="商品描述">
            <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="200" placeholder="选填" />
          </el-form-item>
          <el-form-item label="SKU">
            <el-table :data="editSkus" size="small" border class="pa-sku-table">
              <el-table-column prop="specText" label="规格组合" min-width="140" />
              <el-table-column label="价格（元）" width="170">
                <template #default="{ row }">
                  <el-input-number v-model="row.price" :min="0" :precision="2" size="small" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="库存" width="150">
                <template #default="{ row }">
                  <el-input-number v-model="row.stock" :min="0" size="small" controls-position="right" />
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pa-page {
  padding: 20px;
}
.pa-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}
.pa-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.pa-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.pa-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.pa-sku-table {
  width: 100%;
}
</style>