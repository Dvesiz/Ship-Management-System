<template>
  <div class="ship-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">船舶管理</h2>
        <p class="page-subtitle">管理所有船舶信息</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增船舶</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索船名"
          clearable
          prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="在役" value="在役" />
          <el-option label="维修中" value="维修中" />
          <el-option label="停运" value="停运" />
          <el-option label="航行中" value="航行中" />
        </el-select>
        <el-select v-model="searchForm.categoryId" placeholder="类型" clearable filterable style="width: 140px">
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="tableData"
            :fetchAllDataFn="fetchAllShips"
            filename="船舶管理"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <div class="batch-actions-bar" v-if="selectedRows.length > 0">
        <el-tag type="info" effect="dark">
          已选择 <strong>{{ selectedRows.length }}</strong> 项
        </el-tag>
        <el-button type="danger" size="small" :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
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
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImg"
              :src="row.coverImg"
              :preview-src-list="[row.coverImg]"
              style="width: 48px; height: 48px; border-radius: 8px"
              preview-teleported
              fit="cover"
              class="ship-cover"
            />
            <div v-else class="no-cover">
              <el-icon><Ship /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="船名" min-width="120" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="tonnage" label="吨位 (T)" width="100" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
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

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无船舶数据">
        <el-button type="primary" @click="openDialog('add')">新增船舶</el-button>
      </el-empty>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="500px" class="detail-drawer">
      <div class="detail-content" v-if="currentRow">
        <div class="detail-cover">
          <el-image
            v-if="currentRow.coverImg"
            :src="currentRow.coverImg"
            fit="cover"
            class="cover-image"
          />
          <div v-else class="no-cover-large">
            <el-icon :size="48"><Ship /></el-icon>
          </div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="船舶名称">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="船舶类型">{{ getCategoryName(currentRow.categoryId) }}</el-descriptions-item>
          <el-descriptions-item label="总吨位">{{ currentRow.tonnage }} 吨</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentRow.status)" size="small">
              {{ getStatusLabel(currentRow.status) }}
            </el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="船舶名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入船名" />
        </el-form-item>
        <el-form-item label="类型" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="吨位" prop="tonnage">
          <el-input-number v-model="form.tonnage" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在役" value="在役" />
            <el-option label="维修中" value="维修中" />
            <el-option label="停运" value="停运" />
            <el-option label="航行中" value="航行中" />
          </el-select>
        </el-form-item>
        <el-form-item label="船舶照片">
          <el-upload
            action="#"
            :http-request="customUpload"
            :show-file-list="false"
            class="avatar-uploader"
          >
            <img v-if="form.coverImg" :src="form.coverImg" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
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
import { Plus, Search, Edit, Delete, Refresh, View, Ship, Download } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'

const loading = ref(false)
const tableData = ref([])
const tableKey = ref(0)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const dialogTitle = ref('')
const drawerTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const categoryOptions = ref([])
const selectedRows = ref([])
const currentRow = ref(null)

const searchForm = reactive({ name: '', status: '', categoryId: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const form = ref({ id: null, name: '', categoryId: null, tonnage: 0, status: '在役', coverImg: '' })

const rules = {
  name: [{ required: true, message: '请输入船名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/ship-categories')
    if (res.code === 0) {
      categoryOptions.value = res.data.map(item => ({ label: item.name, value: item.id }))
    }
  } catch(e) { console.error('获取分类失败', e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/ship', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        name: searchForm.name,
        status: searchForm.status,
        categoryId: searchForm.categoryId || undefined
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
  searchForm.status = ''
  searchForm.categoryId = ''
  pagination.pageNum = 1
  fetchData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条船舶吗？`, '批量删除', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.delete('/ship/batch', { data: { ids } })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('批量删除失败') }
}

const openDialog = (type, row) => {
  dialogTitle.value = type === 'add' ? '新增船舶' : '编辑船舶'
  form.value = type === 'add'
    ? { id: null, name: '', categoryId: null, tonnage: 0, status: '在役', coverImg: '' }
    : { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  currentRow.value = row
  drawerTitle.value = '船舶详情'
  drawerVisible.value = true
}

const openEditFromDrawer = () => {
  drawerVisible.value = false
  openDialog('edit', currentRow.value)
}

const customUpload = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' }})
    form.value.coverImg = res.data
    ElMessage.success('上传成功')
  } catch(e) { ElMessage.error('上传失败') }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) await request.put('/ship', form.value)
        else await request.post('/ship', form.value)
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch(e) {}
      finally { submitting.value = false }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除船舶 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      await request.delete('/ship', { params: { id: row.id } })
      ElMessage.success('删除成功')
      fetchData()
    })
}

const getCategoryName = (id) => {
  const found = categoryOptions.value.find(c => c.value === id)
  return found ? found.label : id
}

const getStatusLabel = (s) => ({ '在役': '在役', '维修中': '维修中', '停运': '停运', '航行中': '航行中' }[s] || s)
const getStatusType = (s) => ({
  '在役': 'success', '维修中': 'warning', '停运': 'danger', '航行中': 'info'
}[s] || 'primary')

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 导出配置
const exportHeaders = [
  { key: 'id', title: 'ID', width: 60 },
  { key: 'name', title: '船名', width: 120 },
  { key: 'categoryId', title: '类型ID', width: 80 },
  { key: 'tonnage', title: '吨位(T)', width: 100 },
  { key: 'status', title: '状态', width: 100 },
  { key: 'coverImg', title: '封面图片', width: 150 },
  { key: 'createdAt', title: '创建时间', width: 160 },
  { key: 'updatedAt', title: '更新时间', width: 160 }
]

// 获取全部数据用于导出
const fetchAllShips = async () => {
  const res = await request.get('/ship', { params: { pageNum: 1, pageSize: 1000 } })
  return res.data?.records || []
}

onMounted(() => { fetchCategories(); fetchData() })
</script>

<style scoped>
.ship-list-container { padding: 0; }

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
  margin-bottom: 20px;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.search-bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.search-input { width: 200px; }

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

.ship-cover { cursor: pointer; transition: transform 0.3s; }
.ship-cover:hover { transform: scale(1.1); }

.no-cover {
  width: 48px;
  height: 48px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.no-cover-large {
  width: 100%;
  height: 200px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

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

.avatar-uploader {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.avatar-uploader .el-upload {
  border: 2px dashed var(--border-color);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--transition);
  background: var(--bg-secondary);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.avatar-uploader-icon {
  font-size: 32px;
  color: var(--primary-color);
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: var(--border-radius-md);
}

.detail-content { padding: 0 20px; }

.detail-cover {
  margin-bottom: 20px;
  border-radius: var(--border-radius-md);
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 200px;
  display: block;
}
</style>
