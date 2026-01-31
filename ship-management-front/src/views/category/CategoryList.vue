<template>
  <div class="category-list-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">船舶分类</h2>
        <p class="page-subtitle">管理船舶类型分类</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增类型</el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card">
      <div class="search-bar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增类型</el-button>
        </div>
        <div class="search-bar-right">
          <ExportButtons
            :currentData="tableData"
            :fetchAllDataFn="fetchAllData"
            filename="船舶分类"
            :headers="exportHeaders"
            size="default"
          />
        </div>
      </div>

      <!-- 船舶分类列表表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" sortable />
        <el-table-column prop="name" label="类型名称" />
        <el-table-column prop="alias" label="类型别名/代码" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openDialog('edit', row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：散货船" />
        </el-form-item>
        <el-form-item label="别名" prop="alias">
          <el-input v-model="form.alias" placeholder="例如：BULK_CARRIER" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </div>
      </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
/**
 * @file 船舶分类列表页面组件
 * @description 实现船舶分类的增删改查功能，包括列表展示、新增、编辑和删除
 */

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Download } from '@element-plus/icons-vue'
import request from '../../utils/request'
import ExportButtons from '@/components/ExportButtons.vue'

// 状态变量
const loading = ref(false)                // 列表加载状态
const tableData = ref([])                 // 分类列表数据
const dialogVisible = ref(false)            // 对话框显示状态
const dialogTitle = ref('')                // 对话框标题
const submitting = ref(false)              // 提交状态
const formRef = ref(null)                  // 表单引用

// 导出配置
const exportHeaders = [
  { key: 'id', title: 'ID', width: 80 },
  { key: 'name', title: '类型名称', width: 150 },
  { key: 'alias', title: '类型别名', width: 150 }
]

// 获取全部数据用于导出
const fetchAllData = async () => {
  const res = await request.get('/ship-categories')
  return res.data || []
}

// 编辑表单数据
const form = ref({ id: null, name: '', alias: '' })

// 表单验证规则
const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

/**
 * 获取分类列表数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/ship-categories')
    // 【修复】手动按 ID 升序排列（从小到大）
    if (res.data && Array.isArray(res.data)) {
      tableData.value = res.data.sort((a, b) => a.id - b.id)
    } else {
      tableData.value = []
    }
  } finally { loading.value = false }
}

/**
 * 打开新增/编辑对话框
 * @param {string} type - 操作类型：'add' 或 'edit'
 * @param {Object} row - 编辑时的行数据
 */
const openDialog = (type, row) => {
  dialogTitle.value = type === 'add' ? '新增类型' : '编辑类型'
  form.value = type === 'add' ? { id: null, name: '', alias: '' } : { ...row }
  dialogVisible.value = true
}

/**
 * 提交表单（新增或编辑）
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) await request.put('/ship-categories', form.value)
        else await request.post('/ship-categories', form.value)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
      finally { submitting.value = false }
    }
  })
}

/**
 * 删除分类
 */
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '警告', { type: 'warning' }).then(async () => {
    await request.delete('/ship-categories', { params: { id: row.id } })
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 组件挂载时初始化数据
onMounted(fetchData)
</script>

<style scoped>
.category-list-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; justify-content: space-between; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

/* Remove explicit table styling overrides to use global ones if possible, or keep minimal */
</style>
