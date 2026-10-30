<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getCategoryTree } from '@/api/product'
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

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref(0)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  parentId: undefined as number | undefined,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const openCreate = (parent?: Category) => {
  dialogMode.value = 'create'
  editingId.value = 0
  form.name = ''
  form.parentId = parent?.id
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const openEdit = (cat: Category) => {
  dialogMode.value = 'edit'
  editingId.value = cat.id
  form.name = cat.name
  form.parentId = cat.parentId
  formRef.value?.clearValidate()
  dialogVisible.value = true
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
      })
      ElMessage.success('创建成功')
    } else {
      await updateCategory(editingId.value, { name: form.name })
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
</style>
