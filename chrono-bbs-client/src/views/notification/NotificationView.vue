<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getNotifications,
  getNotificationUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  type NotificationItem,
} from '@/api/notification'

const router = useRouter()

const loading = ref(false)
const notifications = ref<NotificationItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const unreadOnly = ref(false)
const unreadCount = ref(0)

const formatTime = (value: string) => (value ? value.replace('T', ' ').slice(0, 19) : '')

const typeMeta = (type: number) => {
  switch (type) {
    case 1:
      return { label: '订单', icon: '📦', className: 'type-order' }
    case 3:
      return { label: '售后', icon: '🧾', className: 'type-after-sale' }
    default:
      return { label: '系统', icon: '🔔', className: 'type-system' }
  }
}

const notifyUnreadChanged = () => {
  window.dispatchEvent(new Event('notification-unread-changed'))
}

const loadUnreadCount = async () => {
  try {
    const res = await getNotificationUnreadCount()
    unreadCount.value = res.data.count || 0
  } catch {
    unreadCount.value = 0
  }
}

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications({
      page: page.value,
      size,
      unreadOnly: unreadOnly.value || undefined,
    })
    notifications.value = res.data.list || []
    total.value = res.data.total
    loadUnreadCount()
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  loadNotifications()
}

const handleOpen = async (item: NotificationItem) => {
  if (item.isRead === 0) {
    try {
      await markNotificationRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      notifyUnreadChanged()
    } catch {
      // 已读失败不阻断跳转
    }
  }
  if (item.relatedType === 1 && item.relatedId) {
    router.push(`/order/${item.relatedId}`)
  }
}

const handleReadAll = async () => {
  try {
    const res = await markAllNotificationsRead()
    const changed = res.data || 0
    notifications.value.forEach((item) => (item.isRead = 1))
    unreadCount.value = 0
    notifyUnreadChanged()
    ElMessage.success(changed > 0 ? `已将 ${changed} 条通知标记为已读` : '暂无未读通知')
    if (unreadOnly.value) loadNotifications()
  } catch {
    // 错误提示由拦截器统一处理
  }
}

onMounted(loadNotifications)
</script>

<template>
  <div class="nt-page">
    <div class="nt-header">
      <div>
        <h2>消息通知</h2>
        <p class="nt-subtitle">订单提交、支付、发货等动态会在这里通知你</p>
      </div>
      <div class="nt-actions">
        <el-radio-group v-model="unreadOnly" size="small" @change="handleFilterChange">
          <el-radio-button :value="false">全部</el-radio-button>
          <el-radio-button :value="true">未读{{ unreadCount ? ` (${unreadCount})` : '' }}</el-radio-button>
        </el-radio-group>
        <el-button size="small" :disabled="total === 0" @click="handleReadAll">全部已读</el-button>
      </div>
    </div>

    <div v-loading="loading" class="nt-list">
      <el-empty v-if="notifications.length === 0" description="暂无通知" />
      <div
        v-for="item in notifications"
        :key="item.id"
        :class="['nt-item', { unread: item.isRead === 0 }]"
        @click="handleOpen(item)"
      >
        <div :class="['nt-icon', typeMeta(item.type).className]">{{ typeMeta(item.type).icon }}</div>
        <div class="nt-body">
          <div class="nt-title-row">
            <span class="nt-title">{{ item.title }}</span>
            <el-tag size="small" effect="plain">{{ typeMeta(item.type).label }}</el-tag>
            <span v-if="item.isRead === 0" class="nt-dot" />
          </div>
          <p class="nt-content">{{ item.content }}</p>
          <span class="nt-time">{{ formatTime(item.createdAt) }}</span>
        </div>
      </div>
    </div>

    <div v-if="total > size" class="nt-pagination">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="loadNotifications"
      />
    </div>
  </div>
</template>

<style scoped>
.nt-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.nt-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.nt-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.nt-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #999;
}

.nt-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nt-list {
  min-height: 200px;
  background: #fff;
  border-radius: 10px;
  padding: 4px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.nt-item {
  display: flex;
  gap: 14px;
  padding: 16px 8px;
  border-bottom: 1px solid #f3f3f3;
  cursor: pointer;
  transition: background 0.2s;
}

.nt-item:last-child {
  border-bottom: none;
}

.nt-item:hover {
  background: #fafafa;
}

.nt-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  background: #f5f7fa;
}

.type-order {
  background: #fff2ef;
}

.type-after-sale {
  background: #f0f7ff;
}

.nt-body {
  flex: 1;
  min-width: 0;
}

.nt-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nt-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.nt-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e2231a;
}

.nt-content {
  margin: 6px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #666;
}

.nt-time {
  font-size: 12px;
  color: #bbb;
}

.nt-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
