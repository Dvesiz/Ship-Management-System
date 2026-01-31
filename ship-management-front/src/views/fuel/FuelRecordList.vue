<script setup>
/**
 * @file 燃油记录管理页面
 * @description 管理船舶的燃油消耗记录
 */

import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { Plus, Edit, Delete, Search, Refresh, DataAnalysis, Download, Money, Odometer } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'
import { 
    getFuelRecordsService, 
    addFuelRecordService, 
    updateFuelRecordService, 
    deleteFuelRecordService,
    getFuelOverviewService
} from '@/api/fuel'

// 燃油记录列表
const fuelRecords = ref([])
const loading = ref(false)
const tableKey = ref(0)
const ships = ref([])

// 统计数据
const overview = ref({
    totalQuantity: 0,
    totalCost: 0,
    recordCount: 0
})

// 分页和搜索
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ shipId: '', fuelType: '' })

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)

// 表单数据
const formRef = ref(null)
const formData = ref({
    id: null,
    shipId: null,
    voyageId: null,
    recordDate: '',
    fuelType: 'HFO',
    quantity: null,
    unitPrice: null,
    supplier: '',
    port: '',
    remarks: ''
})

// 表单规则
const formRules = {
    shipId: [{ required: true, message: '请选择船舶', trigger: 'change' }],
    recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
    quantity: [{ required: true, message: '请输入消耗量', trigger: 'blur' }]
}

// 燃油类型选项
const fuelTypeOptions = [
    { label: '重油(HFO)', value: 'HFO' },
    { label: '轻柴油(MDO)', value: 'MDO' },
    { label: '船用汽油(MGO)', value: 'MGO' },
    { label: '液化天然气(LNG)', value: 'LNG' }
]

// 燃油类型标签
const fuelTypeTagMap = {
    'HFO': { type: 'danger', label: '重油' },
    'MDO': { type: 'warning', label: '轻柴油' },
    'MGO': { type: 'success', label: '船用汽油' },
    'LNG': { type: 'primary', label: 'LNG' }
}

// 导出配置
const exportHeaders = [
    { key: 'id', title: 'ID', width: 60 },
    { key: 'shipId', title: '船舶ID', width: 80 },
    { key: 'recordDate', title: '记录日期', width: 110 },
    { key: 'fuelType', title: '燃油类型', width: 100 },
    { key: 'quantity', title: '消耗量(吨)', width: 110 },
    { key: 'unitPrice', title: '单价(元/吨)', width: 120 },
    { key: 'totalCost', title: '总费用', width: 130 },
    { key: 'supplier', title: '供应商', width: 100 },
    { key: 'port', title: '加油港口', width: 100 },
    { key: 'remarks', title: '备注', width: 150 }
]

// 获取全部数据用于导出
const fetchAllFuelRecords = async () => {
    const res = await getFuelRecordsService({ pageNum: 1, pageSize: 1000 })
    return res.data?.records || []
}

// 获取燃油记录列表
const fetchFuelRecords = async () => {
    loading.value = true
    try {
        const res = await getFuelRecordsService({
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize,
            shipId: searchForm.shipId || undefined,
            fuelType: searchForm.fuelType || undefined
        })
        await nextTick()
        fuelRecords.value = res.data.records
        pagination.total = res.data.total
        tableKey.value++
    } catch (error) {
        ElMessage.error('获取燃油记录失败')
    } finally {
        loading.value = false
    }
}

// 获取统计概览
const fetchOverview = async () => {
    try {
        const res = await getFuelOverviewService()
        overview.value = res.data
    } catch (error) {
        console.error('获取统计数据失败', error)
    }
}

// 获取船舶列表
const fetchShips = async () => {
    try {
        const res = await request.get('/ship', { params: { pageNum: 1, pageSize: 100 } })
        ships.value = res.data.records
    } catch (error) {
        console.error('获取船舶列表失败', error)
    }
}

// 分页和搜索处理
const handlePageChange = (val) => {
    pagination.pageNum = val
    fetchFuelRecords()
}
const handleSearch = () => {
    pagination.pageNum = 1
    fetchFuelRecords()
}
const handleReset = () => {
    searchForm.shipId = ''
    searchForm.fuelType = ''
    pagination.pageNum = 1
    fetchFuelRecords()
}

// 弹窗相关
const handleAdd = () => {
    isEdit.value = false
    dialogTitle.value = '新增燃油记录'
    formData.value = {
        id: null,
        shipId: null,
        voyageId: null,
        recordDate: new Date().toISOString().split('T')[0],
        fuelType: 'HFO',
        quantity: null,
        unitPrice: null,
        supplier: '',
        port: '',
        remarks: ''
    }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    isEdit.value = true
    dialogTitle.value = '编辑燃油记录'
    formData.value = { ...row }
    dialogVisible.value = true
}

const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true
            try {
                if (isEdit.value) {
                    await updateFuelRecordService(formData.value)
                    ElMessage.success('更新成功')
                } else {
                    await addFuelRecordService(formData.value)
                    ElMessage.success('添加成功')
                }
                dialogVisible.value = false
                fetchFuelRecords()
                fetchOverview()
            } catch (error) {
                if (error !== false) ElMessage.error('操作失败')
            } finally {
                submitting.value = false
            }
        }
    })
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除这条燃油记录吗？', '提示', { type: 'warning' })
        await deleteFuelRecordService(row.id)
        ElMessage.success('删除成功')
        fetchFuelRecords()
        fetchOverview()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败')
    }
}

const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleDateString('zh-CN')
}

const formatMoney = (value) => {
    if (!value && value !== 0) return '-'
    return '¥' + Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const getShipName = (shipId) => {
    const ship = ships.value.find(s => s.id === shipId)
    return ship?.name || '-'
}

const computedTotalCost = computed(() => {
    if (formData.value.quantity && formData.value.unitPrice) {
        return (formData.value.quantity * formData.value.unitPrice).toFixed(2)
    }
    return '0.00'
})

onMounted(() => {
    fetchFuelRecords()
    fetchOverview()
    fetchShips()
})
</script>

<template>
    <div class="fuel-list-container">
        <div class="page-header">
            <div class="header-left">
                <h2 class="page-title">燃油记录</h2>
                <p class="page-subtitle">管理船舶燃油消耗记录</p>
            </div>
            <div class="header-right">
                <el-button type="primary" :icon="Plus" @click="handleAdd">新增记录</el-button>
            </div>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-blue">
                            <el-icon><DataAnalysis /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ overview.recordCount }}</div>
                            <div class="stat-label">记录总数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-green">
                            <el-icon><Odometer /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ Number(overview.totalQuantity).toFixed(2) }} <span class="unit">吨</span></div>
                            <div class="stat-label">总消耗量</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-orange">
                            <el-icon><Money /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ formatMoney(overview.totalCost) }}</div>
                            <div class="stat-label">总费用</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-card shadow="never" class="list-card">
            <div class="search-bar">
                <el-select v-model="searchForm.shipId" placeholder="选择船舶" clearable filterable style="width: 200px">
                    <el-option v-for="ship in ships" :key="ship.id" :label="ship.name" :value="ship.id" />
                </el-select>
                <el-select v-model="searchForm.fuelType" placeholder="燃油类型" clearable style="width: 150px">
                    <el-option v-for="item in fuelTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
                <div class="search-bar-right">
                    <ExportButtons
                        :currentData="fuelRecords"
                        :fetchAllDataFn="fetchAllFuelRecords"
                        filename="燃油记录"
                        :headers="exportHeaders"
                        size="default"
                    />
                </div>
            </div>

            <el-table
                :data="fuelRecords"
                :key="tableKey"
                v-loading="loading"
                stripe
                style="width: 100%"
            >
                <el-table-column label="船舶" width="120">
                    <template #default="{ row }">
                        {{ getShipName(row.shipId) }}
                    </template>
                </el-table-column>
                <el-table-column label="记录日期" width="120">
                    <template #default="{ row }">
                        {{ formatDate(row.recordDate) }}
                    </template>
                </el-table-column>
                <el-table-column label="燃油类型" width="100">
                    <template #default="{ row }">
                        <el-tag :type="fuelTypeTagMap[row.fuelType]?.type || 'info'" size="small">
                            {{ fuelTypeTagMap[row.fuelType]?.label || row.fuelType }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="quantity" label="消耗量(吨)" width="120">
                    <template #default="{ row }">
                        {{ row.quantity?.toFixed(2) || '-' }}
                    </template>
                </el-table-column>
                <el-table-column prop="unitPrice" label="单价(元/吨)" width="120">
                    <template #default="{ row }">
                        {{ row.unitPrice?.toFixed(2) || '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="总费用" width="140">
                    <template #default="{ row }">
                        <span style="color: var(--danger-color); font-weight: 500;">
                            {{ formatMoney(row.totalCost) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="supplier" label="供应商" width="120" show-overflow-tooltip />
                <el-table-column prop="port" label="加油港口" width="120" show-overflow-tooltip />
                <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
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
                    @size-change="fetchFuelRecords"
                    @current-change="handlePageChange"
                    background
                />
            </div>

            <el-empty v-if="fuelRecords.length === 0 && !loading" description="暂无燃油记录">
                <el-button type="primary" @click="handleAdd">新增记录</el-button>
            </el-empty>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-form-item label="船舶" prop="shipId">
                    <el-select v-model="formData.shipId" placeholder="请选择船舶" filterable style="width: 100%">
                        <el-option v-for="ship in ships" :key="ship.id" :label="ship.name" :value="ship.id" />
                    </el-select>
                </el-form-item>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="记录日期" prop="recordDate">
                            <el-date-picker 
                                v-model="formData.recordDate" 
                                type="date" 
                                placeholder="选择日期"
                                value-format="YYYY-MM-DD"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="燃油类型">
                            <el-select v-model="formData.fuelType" style="width: 100%">
                                <el-option v-for="item in fuelTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="消耗量(吨)" prop="quantity">
                            <el-input-number 
                                v-model="formData.quantity" 
                                :precision="2" 
                                :min="0"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单价(元/吨)">
                            <el-input-number 
                                v-model="formData.unitPrice" 
                                :precision="2" 
                                :min="0"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="预计总费用">
                    <el-input :value="'¥' + computedTotalCost" disabled />
                </el-form-item>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="供应商">
                            <el-input v-model="formData.supplier" placeholder="请输入供应商" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="加油港口">
                            <el-input v-model="formData.port" placeholder="请输入港口" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="备注">
                    <el-input v-model="formData.remarks" type="textarea" :rows="2" placeholder="请输入备注" />
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

<style scoped>
.fuel-list-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.stats-row { margin-bottom: 24px; }

.stat-card { border: 1px solid var(--card-border) !important; background: var(--card-bg) !important; box-shadow: var(--shadow-sm) !important; transition: transform 0.3s, box-shadow 0.3s; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.stat-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md) !important; }

.stat-content { display: flex; align-items: center; gap: 16px; padding: 10px 0; }
.stat-icon { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; }
.bg-blue { background: var(--coral-500); box-shadow: var(--shadow-sm); }
.bg-green { background: var(--success); box-shadow: var(--shadow-sm); }
.bg-orange { background: var(--warning); box-shadow: var(--shadow-sm); }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--text-primary); line-height: 1.2; }
.stat-value .unit { font-size: 14px; font-weight: normal; color: var(--text-secondary); margin-left: 4px; }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }

.list-card { min-height: calc(100vh - 300px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
