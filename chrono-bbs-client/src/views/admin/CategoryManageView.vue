<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadUserFile } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getCategoryTree, getCategoryChildren } from '@/api/product'
import { uploadImage } from '@/api/upload'
import { createCategory, updateCategory, deleteCategory } from '@/api/admin'
import type { Category } from '@/types/product'

const loading = ref(false)
const categories = ref<Category[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {
    categories.value = []
  } finally {
    loading.value = false
  }
}

const levelText = (level: number) => ({ 1: '一级', 2: '二级', 3: '三级' }[level] || '')

const findCategory = (id: number, list: Category[] = categories.value): Category | null => {
  for (const c of list) {
    if (c.id === id) return c
    if (c.children?.length) {
      const found = findCategory(id, c.children)
      if (found) return found
    }
  }
  return null
}

// 查找分类的父级 ID；根分类返回 0，未找到返回 -1
const findParentId = (id: number, list: Category[] = categories.value, parentId = 0): number => {
  for (const c of list) {
    if (c.id === id) return parentId
    if (c.children?.length) {
      const found = findParentId(id, c.children, c.id)
      if (found !== -1) return found
    }
  }
  return -1
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref(0)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  parentId: undefined as number | undefined,
  sortOrder: 0,
})

const iconUrl = ref('')
const iconFileList = ref<UploadUserFile[]>([])

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const openCreate = (parent?: Category) => {
  dialogMode.value = 'create'
  editingId.value = 0
  form.name = ''
  form.parentId = parent?.id
  form.sortOrder = 0
  iconUrl.value = ''
  iconFileList.value = []
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const openEdit = async (cat: Category) => {
  dialogMode.value = 'edit'
  editingId.value = cat.id
  form.name = cat.name
  form.parentId = cat.parentId
  form.sortOrder = 0
  iconUrl.value = cat.icon || ''
  iconFileList.value = cat.icon ? [{ name: '图标', url: cat.icon }] : []
  formRef.value?.clearValidate()
  dialogVisible.value = true
  // 分类树接口不返回排序值，按父级子分类列表补齐
  const parentId = findParentId(cat.id)
  if (parentId >= 0) {
    try {
      const res = await getCategoryChildren(parentId)
      form.sortOrder = (res.data || []).find((item) => item.id === cat.id)?.sortOrder ?? 0
    } catch {
      form.sortOrder = 0
    }
  }
}

const handleIconChange = async (file: UploadUserFile) => {
  if (!file.raw) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.raw.type)) {
    ElMessage.warning('仅支持 jpg/jpeg/png/webp 格式')
    iconFileList.value = iconFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    iconFileList.value = iconFileList.value.filter((item) => item.uid !== file.uid)
    return
  }
  try {
    const res = await uploadImage(file.raw)
    file.url = res.data.url
    iconUrl.value = res.data.url
  } catch {
    iconFileList.value = iconFileList.value.filter((item) => item.uid !== file.uid)
  }
}

const handleIconRemove = () => {
  iconUrl.value = ''
}

const handleDelete = async (cat: Category) => {
  if (cat.children?.length) {
    ElMessage.warning('该分类下还有子分类，请先删除子分类')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除分类「${cat.name}」吗？`, '删除分类', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteCategory(cat.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 错误提示由拦截器统一处理
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      const parent = form.parentId ? findCategory(form.parentId) : null
      await createCategory({
        name: form.name,
        parentId: parent?.id ?? 0,
        level: parent ? parent.level + 1 : 1,
        sortOrder: form.sortOrder,
        icon: iconUrl.value || undefined,
      })
      ElMessage.success('创建成功')
    } else {
      // 后端约束：编辑仅允许修改名称、排序、图标，父级/层级不可变更
      await updateCategory(editingId.value, {
        name: form.name,
        sortOrder: form.sortOrder,
        icon: iconUrl.value || undefined,
      })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadData() })
</script>

<template>
  <div class="cm-page">
    <div class="cm-header">
      <h2>分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate()">新增分类</el-button>
    </div>

    <div v-loading="loading" class="cm-card">
      <el-tree
        :data="categories"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        default-expand-all
      >
        <template #default="{ data }">
          <div class="cat-node">
            <img v-if="data.icon" :src="data.icon" class="cat-icon" alt="" />
            <span class="cat-name">{{ data.name }}</span>
            <el-tag size="small" type="info" effect="plain">{{ levelText(data.level) }}</el-tag>
            <div class="cat-actions">
              <el-button
                v-if="data.level < 3"
                text
                size="small"
                :icon="Plus"
                @click="openCreate(data)"
              >
                添加子分类
              </el-button>
              <el-button text size="small" :icon="Edit" @click="openEdit(data)">编辑</el-button>
              <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(data)">
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
      <div v-if="categories.length === 0 && !loading" class="cm-empty">
        <p>暂无分类数据</p>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增分类' : '编辑分类'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
          <span class="cm-sort-hint">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="分类图标">
          <el-upload
            v-model:file-list="iconFileList"
            list-type="picture-card"
            :auto-upload="false"
            :limit="1"
            :on-change="handleIconChange"
            :on-remove="handleIconRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="父级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categories"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="不选则为顶级分类"
            class="cm-tree-select"
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

.cm-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.cat-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  padding: 4px 0;
}

.cat-name {
  font-size: 14px;
  color: #333;
}

.cat-icon {
  width: 20px;
  height: 20px;
  border-radius: 3px;
  object-fit: cover;
  flex-shrink: 0;
}

.cat-actions {
  margin-left: auto;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.cat-node:hover .cat-actions {
  opacity: 1;
}

.cm-empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.cm-tree-select {
  width: 100%;
}

.cm-sort-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>
