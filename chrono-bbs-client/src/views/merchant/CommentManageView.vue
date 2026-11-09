<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantProducts } from '@/api/merchant'
import { getComments, replyComment } from '@/api/comment'
import type { CommentItem } from '@/types/comment'

interface MerchantProduct {
  id: number
  name: string
}

const products = ref<MerchantProduct[]>([])
const productsLoading = ref(false)
const selectedProductId = ref<number | null>(null)

const comments = ref<CommentItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const commentsLoading = ref(false)

const replyVisible = ref(false)
const replySubmitting = ref(false)
const replyTarget = ref<CommentItem | null>(null)
const replyContent = ref('')

const loadProducts = async () => {
  productsLoading.value = true
  try {
    const res = await getMerchantProducts({ page: 1, size: 100 })
    products.value = (res.data.list || []).map((item: MerchantProduct) => ({ id: item.id, name: item.name }))
    if (products.value.length > 0) {
      selectedProductId.value = products.value[0].id
      loadComments()
    }
  } catch {
    products.value = []
  } finally {
    productsLoading.value = false
  }
}

const loadComments = async () => {
  if (!selectedProductId.value) return
  commentsLoading.value = true
  try {
    const res = await getComments(selectedProductId.value, { page: page.value, size })
    comments.value = res.data.list || []
    total.value = res.data.total
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const handleProductChange = () => {
  page.value = 1
  loadComments()
}

const openReply = (comment: CommentItem) => {
  replyTarget.value = comment
  replyContent.value = ''
  replyVisible.value = true
}

const handleSubmitReply = async () => {
  if (!replyTarget.value || !replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replySubmitting.value = true
  try {
    await replyComment({ parentId: replyTarget.value.id, content: replyContent.value.trim() })
    ElMessage.success('回复成功')
    replyVisible.value = false
    loadComments()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    replySubmitting.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<template>
  <div class="mc-page">
    <div class="mc-header">
      <h2>评论管理</h2>
      <el-select
        v-model="selectedProductId"
        :loading="productsLoading"
        placeholder="选择商品"
        class="mc-select"
        @change="handleProductChange"
      >
        <el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </div>

    <div v-loading="commentsLoading" class="mc-list">
      <el-empty v-if="comments.length === 0" description="暂无评论" />
      <div v-for="item in comments" :key="item.id" class="mc-item">
        <div class="mc-head">
          <el-avatar :size="36" :src="item.avatar">{{ (item.nickname || item.username)?.charAt(0) }}</el-avatar>
          <div class="mc-user">
            <span class="mc-name">{{ item.nickname || item.username }}</span>
            <el-rate :model-value="item.star" disabled size="small" />
          </div>
          <span class="mc-time">{{ item.createdAt }}</span>
        </div>
        <p class="mc-content">{{ item.content }}</p>
        <div v-if="item.append" class="mc-append">追评：{{ item.append.content }}</div>
        <div v-for="reply in item.replies || []" :key="reply.id" class="mc-reply">
          <span class="mc-reply-label">商家回复：</span>{{ reply.content }}
          <span class="mc-reply-time">{{ reply.createdAt }}</span>
        </div>
        <div class="mc-actions">
          <el-button size="small" type="primary" plain @click="openReply(item)">回复</el-button>
        </div>
      </div>
    </div>

    <div v-if="total > size" class="mc-pagination">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadComments"
      />
    </div>

    <el-dialog v-model="replyVisible" title="回复评论" width="480px">
      <p class="mc-target">{{ replyTarget?.content }}</p>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="请输入回复内容"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySubmitting" @click="handleSubmitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.mc-page {
  padding: 20px;
}

.mc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mc-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.mc-select {
  width: 260px;
}

.mc-list {
  background: #fff;
  border-radius: 8px;
  padding: 8px 20px;
  min-height: 200px;
}

.mc-item {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}
.mc-item:last-child {
  border-bottom: none;
}

.mc-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mc-user {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mc-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.mc-time {
  margin-left: auto;
  font-size: 12px;
  color: #bbb;
}

.mc-content {
  margin: 10px 0 0;
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.mc-append,
.mc-reply {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
}

.mc-append {
  background: #f9fafb;
}

.mc-reply {
  background: #fff7f0;
}

.mc-reply-label {
  color: #e2231a;
  font-weight: 600;
}

.mc-reply-time {
  margin-left: 8px;
  color: #bbb;
  font-size: 12px;
}

.mc-actions {
  margin-top: 10px;
  text-align: right;
}

.mc-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.mc-target {
  margin: 0 0 12px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
}
</style>
