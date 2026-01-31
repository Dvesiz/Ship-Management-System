<template>
  <div class="crew-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">船员管理</h2>
        <p class="page-subtitle">管理船员信息和分配</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增船员</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索船员姓名"
          clearable
          prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchForm.shipId" placeholder="所属船舶" clearable filterable style="width: 180px">
          <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="searchForm.position" placeholder="职位" clearable style="width: 140px">
          <el-option label="船长" value="船长" />
          <el-option label="大副" value="大副" />
          <el-option label="二副" value="二副" />
          <el-option label="轮机长" value="轮机长" />
          <el-option label="大管轮" value="大管轮" />
          <el-option label="水手长" value="水手长" />
          <el-option label="水手" value="水手" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="tableData"
            :fetchAllDataFn="fetchAllCrew"
            filename="船员管理"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <div class="batch-actions-bar" v-if="selectedRows.length > 0">
        <el-tag type="info" effect="dark">
          已选择 <strong>{{ selectedRows.length }}</strong> 项
        </el-tag>
        <el-button type="danger" size="small" :icon="Delete" @click="handleBatchDelete">批量删除</el-button>
        <el-button size="small" @click="selectedRows = []">取消</el-button>
      </div>

      <el-table
        :data="tableData"
        :key="tableKey"
        v-loading="loading"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="姓名" min-width="120">
          <template #default="{ row }">
            <div class="crew-name">
              <el-avatar :size="36" class="crew-avatar">
                {{ row.name.charAt(0) }}
              </el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="light">{{ row.position }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属船舶" min-width="150">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !getShipName(row.shipId) }">
              {{ getShipName(row.shipId) || '未分配' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ? formatDate(row.createdAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link :icon="Edit" @click="openDialog('edit', row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="handlePageChange"
          background
        />
      </div>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无船员数据">
        <el-button type="primary" @click="openDialog('add')">新增船员</el-button>
      </el-empty>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="480px" class="detail-drawer">
      <div class="detail-content" v-if="currentRow">
        <div class="detail-header">
          <el-avatar :size="64" class="detail-avatar">
            {{ currentRow.name.charAt(0) }}
          </el-avatar>
          <div class="detail-info">
            <h3>{{ currentRow.name }}</h3>
            <el-tag :type="getPositionType(currentRow.position)" size="small">
              {{ currentRow.position }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="联系电话">{{ currentRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属船舶">
            <el-tag type="success" size="small">{{ getShipName(currentRow.shipId) || '未分配' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentRow.createdAt ? formatDate(currentRow.createdAt) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentRow.updatedAt ? formatDate(currentRow.updatedAt) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEditFromDrawer">编辑</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-select v-model="form.position" placeholder="请选择职位" style="width: 100%">
            <el-option label="船长" value="船长" />
            <el-option label="大副" value="大副" />
            <el-option label="二副" value="二副" />
            <el-option label="轮机长" value="轮机长" />
            <el-option label="大管轮" value="大管轮" />
            <el-option label="水手长" value="水手长" />
            <el-option label="水手" value="水手" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属船舶" prop="shipId">
          <el-select v-model="form.shipId" filterable placeholder="请选择船舶" style="width: 100%">
            <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { Plus, Search, Edit, Delete, Refresh, View, Download } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'

const loading = ref(false)
const tableData = ref([])
const tableKey = ref(0)
const shipOptions = ref([])
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const dialogTitle = ref('')
const drawerTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const selectedRows = ref([])
const currentRow = ref(null)

const searchForm = reactive({ name: '', shipId: '', position: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const form = ref({ id: null, name: '', position: '', shipId: null, phone: '' })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  position: [{ required: true, message: '请选择职位', trigger: 'change' }]
}

const fetchShips = async () => {
  try {
    const res = await request.get('/ship', { params: { pageNum: 1, pageSize: 100 } })
    shipOptions.value = res.data.records.map(s => ({ label: s.name, value: s.id }))
  } catch (e) {}
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/crew', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        name: searchForm.name,
        shipId: searchForm.shipId || undefined,
        position: searchForm.position || undefined
      }
    })
    await nextTick()
    tableData.value = [...res.data.records]
    pagination.total = res.data.total
    tableKey.value++
  } finally { loading.value = false }
}

const handlePageChange = (val) => {
  pagination.pageNum = val
  fetchData()
}

const handleSearch = () => { pagination.pageNum = 1; fetchData() }
const handleReset = () => {
  searchForm.name = ''
  searchForm.shipId = ''
  searchForm.position = ''
  pagination.pageNum = 1
  fetchData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 名船员吗？`, '批量删除', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.delete('/crew/batch', { data: { ids } })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('批量删除失败') }
}

const openDialog = (type, row) => {
  dialogTitle.value = type === 'add' ? '新增船员' : '编辑船员'
  form.value = type === 'add'
    ? { id: null, name: '', position: '', shipId: null, phone: '' }
    : { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  currentRow.value = row
  drawerTitle.value = '船员详情'
  drawerVisible.value = true
}

const openEditFromDrawer = () => {
  drawerVisible.value = false
  openDialog('edit', currentRow.value)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) await request.put('/crew', form.value)
        else await request.post('/crew', form.value)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
      finally { submitting.value = false }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除船员 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      await request.delete('/crew', { params: { id: row.id } })
      ElMessage.success('删除成功')
      fetchData()
    })
}

const getShipName = (id) => shipOptions.value.find(s => s.value === id)?.label || ''
const getPositionType = (position) => {
  const map = { '船长': 'primary', '大副': 'success', '二副': 'warning', '轮机长': 'danger', '大管轮': 'info' }
  return map[position] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 导出配置
const exportHeaders = [
  { key: 'id', title: 'ID', width: 60 },
  { key: 'name', title: '姓名', width: 100 },
  { key: 'position', title: '职位', width: 100 },
  { key: 'shipId', title: '船舶ID', width: 80 },
  { key: 'phone', title: '联系电话', width: 150 },
  { key: 'createdAt', title: '创建时间', width: 160 }
]

// 获取全部数据用于导出
const fetchAllCrew = async () => {
  const res = await request.get('/crew', { params: { pageNum: 1, pageSize: 1000 } })
  return res.data?.records || []
}

onMounted(() => { fetchShips(); fetchData() })
</script>

<style scoped>
.crew-list-container { padding: 0; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.header-left .page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.header-left .page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 16px 20px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.search-bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.search-input { width: 180px; }

.batch-actions-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 107, 107, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
}

.crew-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.crew-avatar {
  background: var(--coral-500);
  color: white;
  font-weight: 600;
}

.text-muted { color: var(--text-tertiary); }

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 16px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-top: none;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.detail-content { padding: 0 20px; }

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.detail-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.detail-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--text-primary);
}
</style>
