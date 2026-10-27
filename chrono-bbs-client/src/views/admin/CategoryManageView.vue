<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategoryTree } from '@/api/product'
import type { Category } from '@/types/product'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

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

const handleAdd = () => {
  ElMessage.info('添加分类（功能待接入后端）')
}

const handleEdit = (cat: Category) => {
  ElMessage.info(`编辑分类「${cat.name}」（功能待接入后端）`)
}

const handleDelete = (cat: Category) => {
  ElMessage.info(`删除分类「${cat.name}」（功能待接入后端）`)
}

onMounted(() => { loadData() })
</script>

<template>
  <div class="cm-page">
    <div class="cm-header">
      <h2>分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
    </div>

    <div v-loading="loading" class="cm-card">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="cat-row"
      >
        <span class="cat-name">{{ cat.name }}</span>
        <span class="cat-id">ID: {{ cat.id }}</span>
        <div class="cat-actions">
          <el-button text size="small" :icon="Edit" @click="handleEdit(cat)">编辑</el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(cat)">
            删除
          </el-button>
        </div>
      </div>
      <div v-if="categories.length === 0 && !loading" class="cm-empty">
        <p>暂无分类数据</p>
      </div>
    </div>
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
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.cat-row {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.cat-row:last-child { border-bottom: none; }
.cat-row:hover { background: #fafafa; }

.cat-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-right: 12px;
}

.cat-id {
  font-size: 12px;
  color: #bbb;
}

.cat-actions {
  margin-left: auto;
  display: flex;
  gap: 4px;
}

.cm-empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>