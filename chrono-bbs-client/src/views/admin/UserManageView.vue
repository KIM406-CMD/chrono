<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsers, setUserStatus } from '@/api/admin'
import type { AdminUser } from '@/api/admin'

const loading = ref(false)
const users = ref<AdminUser[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const roleFilter = ref<number | ''>('')
const statusFilter = ref<number | ''>('')

const roleMap: Record<number, string> = { 1: '买家', 2: '商家', 3: '管理员' }

const loadUsers = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; keyword?: string; role?: number; status?: number } = {
      page: page.value,
      size,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (roleFilter.value !== '') params.role = roleFilter.value
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminUsers(params)
    users.value = res.data.list || []
    total.value = res.data.total
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const handleReset = () => {
  keyword.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  page.value = 1
  loadUsers()
}

const handleToggleStatus = async (user: AdminUser) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${user.nickname}」吗？`, `${action}用户`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await setUserStatus(user.id, newStatus)
    user.status = newStatus
    ElMessage.success(`已${action}用户`)
    loadUsers()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => { loadUsers() })
</script>

<template>
  <div class="um-page">
    <h2>用户管理</h2>

    <div class="um-filter">
      <el-input
        v-model="keyword"
        placeholder="用户名 / 手机号"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="roleFilter" placeholder="角色" clearable style="width: 120px">
        <el-option label="买家" :value="1" />
        <el-option label="商家" :value="2" />
        <el-option label="管理员" :value="3" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="users" v-loading="loading" stripe class="um-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="角色" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 3 ? 'danger' : row.role === 2 ? 'warning' : 'info'" size="small">
            {{ roleMap[row.role] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" min-width="160" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="um-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<style scoped>
.um-page {
  padding: 20px;
}
.um-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}
.um-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.um-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.um-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>