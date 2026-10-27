<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingMerchants, reviewMerchant } from '@/api/admin'

const loading = ref(false)
const merchants = ref<any[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPendingMerchants()
    merchants.value = res.data || []
  } catch {
    merchants.value = []
  } finally {
    loading.value = false
  }
}

const handleApprove = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定审核通过商家「${item.shopName}」吗？`, '审核商家', {
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch {
    return
  }
  try {
    await reviewMerchant(item.id, { approved: true })
    ElMessage.success(`商家「${item.shopName}」审核通过`)
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (item: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核商家', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
    })
    await reviewMerchant(item.id, { approved: false, reason: value })
    ElMessage.success(`已拒绝商家「${item.shopName}」`)
    loadData()
  } catch {
    // cancel
  }
}

onMounted(() => { loadData() })
</script>

<template>
  <div class="ma-page">
    <h2>商家审核</h2>

    <el-table :data="merchants" v-loading="loading" stripe class="ma-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="shopName" label="店铺名称" min-width="150" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column label="状态" width="80" align="center">
        <template #default>
          <el-tag type="warning" size="small">待审核</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
          <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="merchants.length === 0 && !loading" class="ma-empty">
      <p>暂无待审核商家</p>
    </div>
  </div>
</template>

<style scoped>
.ma-page {
  padding: 20px;
}
.ma-page h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #333;
}
.ma-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.ma-empty {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  color: #999;
}
</style>