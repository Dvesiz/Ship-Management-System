<template>
  <div class="operation-log-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">操作日志</h2>
        <p class="page-subtitle">查看系统操作记录</p>
      </div>
      <div class="header-right">
        <el-button type="danger" :icon="Delete" @click="handleCleanLogs">清理日志</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-input v-model="filterUsername" placeholder="用户名" clearable style="width: 150px" @keyup.enter="handleSearch" />
        <el-select v-model="filterModule" placeholder="操作模块" clearable style="width: 150px">
          <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filterOperation" placeholder="操作类型" clearable style="width: 130px">
          <el-option v-for="item in operationOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        
        <div class="search-bar-right">
          <ExportButtons 
             :currentData="logs" 
             :fetchAllDataFn="fetchAllLogs" 
             filename="操作日志" 
             :headers="exportHeaders" 
          />
        </div>
      </div>

      <el-table :data="logs" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operation" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationType(row.operation)" size="small" effect="light">
              {{ row.operation }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="operation-desc">{{ formatOperationDesc(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="130" />
        <el-table-column prop="responseResult" label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.responseResult === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.responseResult === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTime" label="耗时" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'slow-query': row.executionTime > 1000 }">{{ row.executionTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">
          <el-tag type="primary">{{ currentLog.username }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          <el-tag>{{ currentLog.module }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationType(currentLog.operation)">{{ currentLog.operation }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getMethodType(currentLog.method)">{{ currentLog.method }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行结果">
          <el-tag :type="currentLog.responseResult === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.responseResult === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">
          <span :class="{ 'slow-query': currentLog.executionTime > 1000 }">{{ currentLog.executionTime }}ms</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">
          <el-text type="info" style="font-family: monospace; font-size: 12px;">{{ currentLog.requestUrl }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="currentLog.requestParams" type="textarea" :rows="4" readonly style="font-family: monospace; font-size: 12px;" />
        </el-descriptions-item>
        <el-descriptions-item label="浏览器信息" :span="2">
          <el-input v-model="currentLog.userAgent" type="textarea" :rows="2" readonly style="font-size: 12px;" />
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
          <el-alert type="error" :closable="false">
            <el-input v-model="currentLog.errorMsg" type="textarea" :rows="4" readonly />
          </el-alert>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogsService, cleanLogsService } from '@/api/log'
import { Search, Delete, Refresh, View, Download } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'

const logs = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterUsername = ref('')
const filterModule = ref('')
const filterOperation = ref('')
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const moduleOptions = [
  { label: '全部', value: '' },
  { label: '用户管理', value: '用户管理' },
  { label: '船舶管理', value: '船舶管理' },
  { label: '船员管理', value: '船员管理' },
  { label: '航次管理', value: '航次管理' },
  { label: '维修管理', value: '维修管理' },
  { label: '证书管理', value: '证书管理' },
  { label: '燃油管理', value: '燃油管理' }
]

const operationOptions = [
  { label: '全部', value: '' },
  { label: '新增', value: '新增' },
  { label: '更新', value: '更新' },
  { label: '删除', value: '删除' }
]

const exportHeaders = [
    { key: 'id', title: 'ID', width: 60 },
    { key: 'username', title: '操作用户', width: 100 },
    { key: 'module', title: '操作模块', width: 100 },
    { key: 'operation', title: '操作类型', width: 100 },
    { key: 'method', title: '请求方法', width: 80 },
    { key: 'requestUrl', title: '请求URL', width: 200 },
    { key: 'ipAddress', title: 'IP地址', width: 120 },
    { key: 'responseResult', title: '执行结果', width: 100 },
    { key: 'executionTime', title: '耗时(ms)', width: 80 },
    { key: 'createdAt', title: '操作时间', width: 160 }
]

const fetchAllLogs = async () => {
    const res = await getLogsService({ pageNum: 1, pageSize: 10000 })
    return res.data?.records || []
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getLogsService({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: filterUsername.value || undefined,
      module: filterModule.value || undefined,
      operation: filterOperation.value || undefined
    })
    logs.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; fetchLogs() }
const handleReset = () => { filterUsername.value = ''; filterModule.value = ''; filterOperation.value = ''; pageNum.value = 1; fetchLogs() }
const handlePageChange = (page) => { pageNum.value = page; fetchLogs() }
const handleSizeChange = (size) => { pageSize.value = size; pageNum.value = 1; fetchLogs() }
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

const getOperationType = (op) => ({ '新增': 'success', '更新': 'warning', '删除': 'danger' }[op] || 'info')
const getMethodType = (method) => ({ 'GET': 'success', 'POST': 'primary', 'PUT': 'warning', 'DELETE': 'danger' }[method] || 'info')

const formatOperationDesc = (log) => {
  const module = log.module || '未知模块'
  const operation = log.operation || '未知操作'
  
  // 尝试解析请求参数，提取详细信息
  let detail = ''
  try {
    if (log.requestParams) {
      const params = JSON.parse(log.requestParams)
      
      // 根据不同模块提取关键信息
      if (Array.isArray(params) && params.length > 0) {
        const firstParam = params[0]
        
        // 提取名称相关字段
        if (firstParam.name) {
          detail = firstParam.name
        } else if (firstParam.username) {
          detail = firstParam.username
        } else if (firstParam.title) {
          detail = firstParam.title
        } else if (firstParam.certificateName) {
          detail = firstParam.certificateName
        } else if (firstParam.shipId) {
          detail = `船舶ID:${firstParam.shipId}`
        }
      }
    }
  } catch (e) {
    // 解析失败，使用默认描述
  }
  
  // 生成描述文本
  if (detail) {
    return `${operation}了「${detail}」${module}`
  } else {
    return `${operation}了${module}`
  }
}

const handleViewDetail = (log) => {
  currentLog.value = log
  detailDialogVisible.value = true
}

const handleCleanLogs = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入要清理的天数（将删除该天数之前的日志）', '清理日志', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的天数'
    })
    await cleanLogsService(parseInt(value))
    ElMessage.success(`已清理 ${value} 天前的日志`)
    fetchLogs()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('清理失败')
  }
}

onMounted(() => { fetchLogs() })
</script>

<style scoped>
.operation-log-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }
.header-right { display: flex; gap: 12px; }

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.slow-query { color: var(--coral-600); font-weight: 600; }

.operation-desc {
  color: var(--text-primary);
  font-size: 14px;
}

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
