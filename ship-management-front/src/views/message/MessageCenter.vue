<template>
  <div class="message-center-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">消息中心</h2>
        <p class="page-subtitle">查看和管理您的消息通知</p>
      </div>
      <div class="header-right">
        <el-button 
          type="success" 
          :icon="Position" 
          plain 
          class="send-btn"
          @click="openSendDialog"
        >
          发送消息
        </el-button>
        <el-button type="primary" :icon="Check" @click="handleMarkAllAsRead" :disabled="unreadCount === 0">
          全部已读
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-select v-model="filterType" placeholder="消息类型" clearable style="width: 140px" @change="handleFilterChange">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="handleFilterChange">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button :icon="Refresh" @click="handleReset">重置筛选</el-button>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="messages"
            :fetchAllDataFn="fetchAllMessages"
            filename="消息中心"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <div class="message-list" v-loading="loading">
        <div v-if="messages.length === 0" class="empty-state">
          <el-empty description="暂无消息">
            <el-button type="primary" @click="handleRefresh">刷新</el-button>
          </el-empty>
        </div>
        
        <div v-else class="message-items">
          <div 
            v-for="message in messages" 
            :key="message.id" 
            class="message-item"
            :class="{ unread: message.status === 'UNREAD', clickable: true }"
            @click="openDetail(message)"
          >
            <div class="message-icon" :class="message.type.toLowerCase()">
              <el-icon :size="20"><component :is="getTypeIcon(message.type)" /></el-icon>
            </div>
            <div class="message-body">
              <div class="message-header">
                <div class="message-title-row">
                  <el-tag :type="typeTagMap[message.type]?.type || 'info'" size="small" effect="light">
                    {{ typeTagMap[message.type]?.label || message.type }}
                  </el-tag>
                  <span class="message-title">{{ message.title }}</span>
                  <span v-if="message.senderName" class="message-sender">
                     - 来自: {{ message.senderName }}
                  </span>
                </div>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-content text-ellipsis">{{ message.content }}</div>
              <div class="message-actions" @click.stop>
                <el-button 
                  v-if="message.status === 'UNREAD'"
                  type="primary" 
                  size="small" 
                  :icon="View"
                  @click="handleMarkAsRead(message)"
                >
                  已读
                </el-button>
                <el-button 
                  size="small"
                  @click="openDetail(message)"
                >
                  详情
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="Delete"
                  @click="handleDelete(message)"
                >
                  删除
                </el-button>
              </div>
            </div>
            <div v-if="message.status === 'UNREAD'" class="unread-badge">
              <el-badge is-dot />
            </div>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
            background
          />
        </div>
      </div>
    </el-card>

    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="消息详情"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <div v-if="currentMessage" class="detail-container">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentMessage.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="typeTagMap[currentMessage.type]?.type" size="small">{{ typeTagMap[currentMessage.type]?.label }}</el-tag>
            <span class="meta-time">{{ formatTime(currentMessage.createdAt) }}</span>
          </div>
          <div class="detail-sender" v-if="currentMessage.senderName">
            <el-avatar :size="24" :src="currentMessage.senderAvatar" class="sender-avatar">
              {{ currentMessage.senderName.charAt(0) }}
            </el-avatar>
            <span class="sender-name">发件人: {{ currentMessage.senderName }}</span>
          </div>
        </div>
        <div class="detail-content">
          {{ currentMessage.content }}
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="danger" plain @click="handleDeleteInDetail">删除</el-button>
          <el-button 
            type="primary" 
            :icon="Position" 
            @click="handleReply"
            v-if="currentMessage?.senderId && currentMessage.senderId !== 0"
          >
            回复
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发送消息弹窗 -->
    <el-dialog
      v-model="sendDialogVisible"
      :title="replyMode ? '回复消息' : '发送站内信'"
      width="500px"
      append-to-body
      destroy-on-close
      @closed="handleSendDialogClosed"
    >
      <el-form :model="sendForm" label-width="80px" ref="sendFormRef" :rules="sendRules">
        <el-form-item label="接收用户" prop="receiverId">
          <el-select 
            v-model="sendForm.receiverId" 
            placeholder="请搜索并选择用户" 
            filterable 
            remote
            :remote-method="searchUsers"
            :loading="loadingUsers"
            style="width: 100%"
            :disabled="replyMode"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.username + (user.nickname ? ` (${user.nickname})` : '')"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="消息类型" prop="type" v-if="isAdmin && !replyMode">
          <el-select v-model="sendForm.type" placeholder="请选择消息类型" style="width: 100%">
            <el-option label="站内信" value="PERSONAL" />
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="维修提醒" value="MAINTENANCE" />
            <el-option label="航次通知" value="VOYAGE" />
            <el-option label="证书提醒" value="CERTIFICATE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="消息标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入消息标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input 
            v-model="sendForm.content" 
            type="textarea" 
            :rows="5" 
            placeholder="请输入消息内容"
            maxlength="500"
            show-word-limit 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="sendDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Check, Delete, View, Refresh, Document, Setting, Timer, Location, Message, Download, Position } from '@element-plus/icons-vue'
import { 
  getMessagesService, getUnreadCountService, markAsReadService, 
  markAllAsReadService, deleteMessageService, sendMessageService
} from '@/api/message'
import { searchUserService } from '@/api/user'
import ExportButtons from '@/components/ExportButtons.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const messages = ref([])
const loading = ref(false)
const total = ref(0)
const unreadCount = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterType = ref('')
const filterStatus = ref('')
let timer = null 

// 详情查看相关
const detailDialogVisible = ref(false)
const currentMessage = ref(null)

// 发送消息相关
const sendDialogVisible = ref(false)
const sending = ref(false)
const sendFormRef = ref(null)
const userOptions = ref([])
const loadingUsers = ref(false)
const replyMode = ref(false)

const sendForm = reactive({
  receiverId: '',
  title: '',
  content: '',
  type: 'PERSONAL' 
})

const sendRules = {
  receiverId: [{ required: true, message: '请选择接收用户', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const typeOptions = [
  { label: '全部', value: '' },
  { label: '系统通知', value: 'SYSTEM' },
  { label: '维修提醒', value: 'MAINTENANCE' },
  { label: '航次通知', value: 'VOYAGE' },
  { label: '证书提醒', value: 'CERTIFICATE' },
  { label: '站内信', value: 'PERSONAL' }
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '未读', value: 'UNREAD' },
  { label: '已读', value: 'READ' }
]

const typeTagMap = {
  'SYSTEM': { type: 'primary', label: '系统' },
  'MAINTENANCE': { type: 'warning', label: '维修' },
  'VOYAGE': { type: 'success', label: '航次' },
  'CERTIFICATE': { type: 'danger', label: '证书' },
  'PERSONAL': { type: 'info', label: '私信' }
}

const getTypeIcon = (type) => {
  const map = {
    'SYSTEM': Setting,
    'MAINTENANCE': Document,
    'VOYAGE': Location,
    'CERTIFICATE': Timer,
    'PERSONAL': Message
  }
  return map[type] || Bell
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await getMessagesService({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: filterType.value || undefined,
      status: filterStatus.value || undefined
    })
    messages.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCountService()
    unreadCount.value = res.data
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const handleMarkAsRead = async (message) => {
  if (message.status === 'READ') return
  try {
    await markAsReadService(message.id)
    message.status = 'READ'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMarkAllAsRead = async () => {
  if (unreadCount.value === 0) {
    ElMessage.info('没有未读消息')
    return
  }
  try {
    await markAllAsReadService()
    messages.value.forEach(m => m.status = 'READ')
    unreadCount.value = 0
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (message) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', { type: 'warning' })
    await deleteMessageService(message.id)
    ElMessage.success('删除成功')
    fetchMessages()
    if (message.status === 'UNREAD') {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openDetail = (message) => {
  currentMessage.value = message
  detailDialogVisible.value = true
  if (message.status === 'UNREAD') {
    handleMarkAsRead(message)
  }
}

const handleDeleteInDetail = () => {
  handleDelete(currentMessage.value)
  detailDialogVisible.value = false
}

const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }
  loadingUsers.value = true
  try {
    const res = await searchUserService(query)
    userOptions.value = res.data
  } catch (error) {
    console.error('搜索用户失败', error)
  } finally {
    loadingUsers.value = false
  }
}

const openSendDialog = () => {
  replyMode.value = false
  sendDialogVisible.value = true
  sendForm.receiverId = ''
  sendForm.title = ''
  sendForm.content = ''
  sendForm.type = 'PERSONAL' 
  userOptions.value = []
}

const handleReply = () => {
  if (!currentMessage.value || !currentMessage.value.senderId) return

  replyMode.value = true
  sendDialogVisible.value = true

  userOptions.value = [{
    id: currentMessage.value.senderId,
    username: currentMessage.value.senderName || '未知用户'
  }]
  sendForm.receiverId = currentMessage.value.senderId
  sendForm.title = `Re: ${currentMessage.value.title}`
  sendForm.content = ''
}

const handleSendDialogClosed = () => {
  if (!replyMode.value) {
    userOptions.value = []
  }
}

const handleSend = async () => {
  if (!sendFormRef.value) return
  
  await sendFormRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true
      try {
        await sendMessageService(sendForm)
        ElMessage.success('消息发送成功')
        sendDialogVisible.value = false
        if (sendForm.receiverId === userStore.user.id) {
          fetchMessages()
        }
      } catch (error) {
        ElMessage.error('发送失败')
      } finally {
        sending.value = false
      }
    }
  })
}

const handlePageChange = (page) => { pageNum.value = page; fetchMessages() }
const handleFilterChange = () => { pageNum.value = 1; fetchMessages() }
const handleReset = () => { filterType.value = ''; filterStatus.value = ''; pageNum.value = 1; fetchMessages() }
const handleRefresh = () => { fetchMessages(); fetchUnreadCount() }
const formatTime = (time) => { if (!time) return '-'; return new Date(time).toLocaleString('zh-CN') }

const exportHeaders = [
  { key: 'id', title: 'ID', width: 60 },
  { key: 'type', title: '类型', width: 100 },
  { key: 'title', title: '标题', width: 200 },
  { key: 'content', title: '内容', width: 300 },
  { key: 'status', title: '状态', width: 80 },
  { key: 'createdAt', title: '创建时间', width: 160 }
]

const fetchAllMessages = async () => {
  const res = await getMessagesService({ pageNum: 1, pageSize: 1000 })
  return res.data?.records || []
}

onMounted(() => { 
  fetchMessages(); 
  fetchUnreadCount();
  
  timer = setInterval(() => {
    if (!sendDialogVisible.value && !detailDialogVisible.value) {
        getMessagesService({
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          type: filterType.value || undefined,
          status: filterStatus.value || undefined
        }).then(res => {
            messages.value = res.data.records
            total.value = res.data.total
        }).catch(() => {})
        
        getUnreadCountService().then(res => {
            unreadCount.value = res.data
        }).catch(() => {})
    }
  }, 30000);
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.message-center-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.header-right { display: flex; gap: 12px; }
.send-btn { font-weight: 600; }

.list-card { min-height: calc(100vh - 180px); border: none !important; box-shadow: none !important; background: transparent !important; }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--toolbar-bg); border: 1px solid var(--toolbar-border); border-radius: var(--border-radius-md); margin-bottom: 20px; }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.message-list { min-height: 400px; }

.message-items { display: flex; flex-direction: column; gap: 12px; }

.message-item { 
  display: flex; 
  gap: 16px; 
  padding: 16px 20px; 
  background: var(--bg-primary); 
  border: 1px solid var(--border-color); 
  border-radius: var(--border-radius-md); 
  transition: var(--transition);
  position: relative;
  cursor: pointer;
}

.message-item:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.message-item.unread { background: var(--primary-light); border-color: var(--primary-color); }
.message-item.clickable { cursor: pointer; }

.message-icon { 
  width: 44px; 
  height: 44px; 
  border-radius: 12px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  color: white; 
  flex-shrink: 0;
}

.message-icon.system { background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%); }
.message-icon.maintenance { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
.message-icon.voyage { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
.message-icon.certificate { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); }
.message-icon.personal { background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); }

.message-body { flex: 1; min-width: 0; }

.message-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.message-title-row { display: flex; align-items: center; gap: 10px; }
.message-title { font-weight: 500; color: var(--text-primary); }
.message-sender { font-size: 13px; color: var(--text-tertiary); font-weight: normal; }
.message-time { font-size: 12px; color: var(--text-tertiary); }

.message-content { color: var(--text-secondary); font-size: 14px; line-height: 1.6; margin-bottom: 12px; }
.text-ellipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.message-actions { display: flex; gap: 8px; opacity: 0; transition: opacity 0.2s; }
.message-item:hover .message-actions { opacity: 1; }

.unread-badge { position: absolute; top: 12px; right: 12px; }

.empty-state { padding: 60px 0; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--toolbar-bg); border: 1px solid var(--toolbar-border); border-top: none; border-radius: 0 0 var(--border-radius-md) var(--border-radius-md); }

/* 详情弹窗样式 */
.detail-container { padding: 10px 0; }
.detail-header { margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid var(--border-light); }
.detail-title { font-size: 20px; font-weight: 600; margin: 0 0 12px 0; color: var(--text-primary); }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.meta-time { color: var(--text-tertiary); font-size: 13px; }

.detail-sender { display: flex; align-items: center; gap: 8px; background: var(--bg-tertiary); padding: 8px 12px; border-radius: 8px; width: fit-content; }
.sender-name { font-size: 13px; color: var(--text-secondary); font-weight: 500; }

.detail-content { font-size: 15px; line-height: 1.8; color: var(--text-primary); white-space: pre-wrap; min-height: 100px; }
</style>
