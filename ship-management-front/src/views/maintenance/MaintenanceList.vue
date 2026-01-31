<template>
  <div class="maintenance-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">维修保养</h2>
        <p class="page-subtitle">管理船舶维修记录</p>
      </div>
      <div class="header-right">
        <el-button type="warning" :icon="Tools" @click="openDialog">新增维修记录</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <el-select v-model="searchForm.shipId" placeholder="选择船舶" clearable filterable style="width: 200px">
          <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="searchForm.dateRange" placeholder="时间范围" clearable style="width: 150px">
          <el-option label="最近一周" value="week" />
          <el-option label="最近一月" value="month" />
          <el-option label="最近三月" value="quarter" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="tableData"
            :fetchAllDataFn="fetchAllMaintenance"
            filename="维修记录"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <el-table :data="tableData" :key="tableKey" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="船舶" min-width="150">
          <template #default="{ row }">
            <div class="maintenance-ship">
              <el-icon><Ship /></el-icon>
              <span>{{ getShipName(row.shipId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="维修内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="费用" width="120">
          <template #default="{ row }">
            <span class="cost-text">¥ {{ Number(row.cost).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">
            {{ row.maintenanceTime ? formatDate(row.maintenanceTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
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

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无维修记录">
        <el-button type="warning" @click="openDialog">新增维修记录</el-button>
      </el-empty>
    </el-card>

    <el-dialog v-model="dialogVisible" title="登记维修" width="540px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="维修船舶" prop="shipId">
          <el-select v-model="form.shipId" filterable placeholder="请选择船舶" style="width: 100%">
            <el-option v-for="item in shipOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修内容" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述维修详情" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="费用 (元)" prop="cost">
              <el-input-number v-model="form.cost" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维修时间" prop="maintenanceTime">
              <el-date-picker v-model="form.maintenanceTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="warning" :loading="submitting" @click="handleSubmit">保存记录</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { Plus, Search, Edit, Delete, Refresh, Tools, Ship, Download } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'

const loading = ref(false)
const tableData = ref([])
const tableKey = ref(0)
const shipOptions = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const searchForm = reactive({ shipId: '', dateRange: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const form = ref({ shipId: null, description: '', cost: 0, maintenanceTime: null })

const rules = {
  shipId: [{ required: true, message: '请选择船舶', trigger: 'change' }],
  description: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  cost: [{ required: true, message: '请输入费用', trigger: 'blur' }]
}

const fetchShips = async () => {
  const res = await request.get('/ship', { params: { pageNum: 1, pageSize: 100 } })
  shipOptions.value = res.data.records.map(s => ({ label: s.name, value: s.id }))
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/maintenance', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        shipId: searchForm.shipId || undefined
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
const handleReset = () => { searchForm.shipId = ''; searchForm.dateRange = ''; pagination.pageNum = 1; fetchData() }

const openDialog = () => {
  form.value = { shipId: null, description: '', cost: 0, maintenanceTime: null }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await request.post('/maintenance', form.value)
        ElMessage.success('登记成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
      finally { submitting.value = false }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除此记录？', '提示', { type: 'warning' }).then(async () => {
    await request.delete('/maintenance', { params: { id: row.id } })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const getShipName = (id) => shipOptions.value.find(s => s.value === id)?.label || id
const formatDate = (dateStr) => { if (!dateStr) return '-'; return new Date(dateStr).toLocaleString('zh-CN') }

// 导出配置
const exportHeaders = [
  { key: 'id', title: 'ID', width: 60 },
  { key: 'shipId', title: '船舶ID', width: 80 },
  { key: 'description', title: '维修内容', width: 200 },
  { key: 'cost', title: '费用(元)', width: 120 },
  { key: 'maintenanceTime', title: '维修时间', width: 160 }
]

// 获取全部数据用于导出
const fetchAllMaintenance = async () => {
  const res = await request.get('/maintenance', { params: { pageNum: 1, pageSize: 1000 } })
  return res.data?.records || []
}

onMounted(() => { fetchShips(); fetchData() })
</script>

<style scoped>
.maintenance-list-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.maintenance-ship { display: flex; align-items: center; gap: 8px; }

.cost-text { color: var(--coral-600); font-weight: 600; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
