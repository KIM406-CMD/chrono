<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getConversations, getMessages, sendMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import type { Conversation, ChatMessage } from '@/api/chat'

const route = useRoute()
const userStore = useUserStore()
const conversations = ref<Conversation[]>([])
const messages = ref<ChatMessage[]>([])
const activeConv = ref<number>(0)
const inputText = ref('')
const loading = ref(false)
const msgListRef = ref<HTMLElement | null>(null)

const loadConversations = async () => {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
    if (conversations.value.length > 0 && !activeConv.value) {
      activeConv.value = conversations.value[0].id
      loadMessages(activeConv.value)
    }
  } catch {
    conversations.value = []
  }
}

const loadMessages = async (convId: number) => {
  loading.value = true
  try {
    const res = await getMessages(convId)
    messages.value = res.data.list || []
    scrollToBottom()
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

const selectConv = (conv: Conversation) => {
  activeConv.value = conv.id
  loadMessages(conv.id)
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || !activeConv.value) return
  const conv = conversations.value.find((c) => c.id === activeConv.value)
  if (!conv) return
  inputText.value = ''
  try {
    const res = await sendMessage({ targetId: conv.targetId, content: text, type: 'text' })
    messages.value.push(res.data)
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
  }
}

onMounted(() => {
  loadConversations()
  const convId = route.params.conversationId
  if (convId) {
    activeConv.value = Number(convId)
    loadMessages(activeConv.value)
  }
})
</script>

<template>
  <div class="chat-page">
    <aside class="chat-sidebar">
      <div class="sidebar-header">消息列表</div>
      <div class="conv-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: conv.id === activeConv }"
          @click="selectConv(conv)"
        >
          <el-avatar :size="36" :src="conv.targetAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="conv-info">
            <div class="conv-name">
              {{ conv.targetName }}
              <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" class="unread-badge" />
            </div>
            <div class="conv-last">{{ conv.lastMessage }}</div>
          </div>
        </div>
      </div>
    </aside>

    <section class="chat-body" v-loading="loading">
      <template v-if="activeConv">
        <div class="chat-top">
          <span>{{ conversations.find((c) => c.id === activeConv)?.targetName || '聊天' }}</span>
        </div>

        <div ref="msgListRef" class="msg-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="{ mine: msg.senderId === userStore.userInfo?.id }"
          >
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="messages.length === 0 && !loading" class="no-msg">
            暂无消息，发送一条消息开始对话吧
          </div>
        </div>

        <div class="chat-input-bar">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button type="primary" class="send-btn" @click="handleSend">发送</el-button>
        </div>
      </template>

      <div v-else class="no-conv">
        <el-empty description="选择一个对话开始聊天" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.chat-sidebar {
  width: 280px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  font-size: 16px;
  font-weight: 600;
  padding: 16px;
  border-bottom: 1px solid #eee;
  color: #333;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover,
.conv-item.active {
  background: #f5f7fa;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.unread-badge {
  flex-shrink: 0;
}

.conv-last {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.chat-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.chat-top {
  font-size: 16px;
  font-weight: 600;
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
  color: #333;
  flex-shrink: 0;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-row {
  display: flex;
}

.msg-row.mine {
  justify-content: flex-end;
}

.msg-row.mine .msg-bubble {
  background: #e2231a;
  color: #fff;
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #f0f0f0;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.no-msg {
  text-align: center;
  color: #ccc;
  margin-top: 40px;
  font-size: 14px;
}

.chat-input-bar {
  padding: 12px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.send-btn {
  align-self: flex-end;
}

.no-conv {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}
</style>