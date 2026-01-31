<template>
  <div class="voyage-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">航次记录</h2>
        <p class="page-subtitle">管理船舶航次信息</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog">新建航次</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-select v-model="searchForm.shipId" placeholder="选择船舶" clearable filterable style="width: 200px">
          <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="计划中" value="计划中" />
          <el-option label="执行中" value="执行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="tableData"
            :fetchAllDataFn="fetchAllVoyages"
            filename="航次记录"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <el-table :data="tableData" :key="tableKey" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="船舶" min-width="150">
          <template #default="{ row }">
            <div class="voyage-ship">
              <el-icon><Ship /></el-icon>
              <span>{{ getShipName(row.shipId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="航线" min-width="200">
          <template #default="{ row }">
            <div class="voyage-route">
              <el-tag type="success" size="small">{{ row.startPort || '-' }}</el-tag>
              <el-icon><ArrowRight /></el-icon>
              <el-tag type="warning" size="small">{{ row.endPort || '-' }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="开航时间" width="160">
          <template #default="{ row }">
            {{ row.startTime ? formatDate(row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="160">
          <template #default="{ row }">
            {{ row.endTime ? formatDate(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="light">
              {{ row.status || '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== '已完成'" type="success" size="small" @click="handleFinish(row)">
              完成
            </el-button>
            <el-tag v-else type="success" size="small">已归档</el-tag>
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

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无航次记录">
        <el-button type="primary" @click="openDialog">新建航次</el-button>
      </el-empty>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建航次" width="540px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="执行船舶" prop="shipId">
          <el-select v-model="form.shipId" filterable placeholder="请选择船舶" style="width: 100%">
            <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="始发港" prop="startPort">
              <el-input v-model="form.startPort" placeholder="请输入始发港" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目的港" prop="endPort">
              <el-input v-model="form.endPort" placeholder="请输入目的港" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开航时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开航时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交开航</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { Plus, Search, Edit, Delete, Refresh, Ship, ArrowRight, Download } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'

const loading = ref(false)
const tableData = ref([])
const tableKey = ref(0)
const shipOptions = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const searchForm = reactive({ shipId: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const form = ref({ shipId: null, startPort: '', endPort: '', startTime: null, remarks: '' })

const rules = {
  shipId: [{ required: true, message: '请选择船舶', trigger: 'change' }],
  startPort: [{ required: true, message: '请输入始发港', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
}

const fetchShips = async () => {
  const res = await request.get('/ship', { params: { pageNum: 1, pageSize: 100 } })
  shipOptions.value = res.data.records.map(s => ({ label: s.name, value: s.id }))
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/voyages', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        shipId: searchForm.shipId || undefined,
        status: searchForm.status || undefined
      }
    })
    await nextTick()
    tableData.value = [...res.data.records]
    pagination.total = res.data.total
    tableKey.value++
  } finally { loading.value = false }
}

const handlePageChange = (val) => { pagination.pageNum = val; fetchData() }
const handleSearch = () => { pagination.pageNum = 1; fetchData() }
const handleReset = () => { searchForm.shipId = ''; searchForm.status = ''; pagination.pageNum = 1; fetchData() }

const openDialog = () => {
  form.value = { shipId: null, startPort: '', endPort: '', startTime: null, remarks: '' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = { ...form.value, startTime: form.value.startTime?.toISOString() }
        await request.post('/voyages', submitData)
        ElMessage.success('开航成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
      finally { submitting.value = false }
    }
  })
}

const handleFinish = (row) => {
  ElMessageBox.confirm('确认结束该航次？', '提示', { type: 'warning' }).then(async () => {
    await request.patch('/voyages/finish', null, { params: { id: row.id } })
    ElMessage.success('航次已完工')
    fetchData()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该航次记录？此操作不可恢复！', '提示', { 
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/voyages/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const getShipName = (id) => shipOptions.value.find(s => s.value === id)?.label || id
const getStatusType = (status) => ({ '已完成': 'success', '执行中': 'warning', '计划中': 'info' }[status] || 'info')
const formatDate = (dateStr) => { if (!dateStr) return '-'; return new Date(dateStr).toLocaleString('zh-CN') }

// 导出配置
const exportHeaders = [
  { key: 'id', title: 'ID', width: 60 },
  { key: 'shipId', title: '船舶ID', width: 80 },
  { key: 'startPort', title: '始发港', width: 100 },
  { key: 'endPort', title: '目的港', width: 100 },
  { key: 'startTime', title: '开航时间', width: 160 },
  { key: 'endTime', title: '结束时间', width: 160 },
  { key: 'status', title: '状态', width: 100 },
  { key: 'remarks', title: '备注', width: 150 }
]

// 获取全部数据用于导出
const fetchAllVoyages = async () => {
  const res = await request.get('/voyages', { params: { pageNum: 1, pageSize: 1000 } })
  return res.data?.records || []
}

onMounted(() => { fetchShips(); fetchData() })
</script>

<style scoped>
.voyage-list-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.voyage-ship { display: flex; align-items: center; gap: 8px; }
.voyage-route { display: flex; align-items: center; gap: 8px; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
