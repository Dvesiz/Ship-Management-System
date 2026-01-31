<script setup>
/**
 * @file 船舶证书管理页面
 * @description 管理船舶的各类证书，支持增删改查和到期提醒
 */

import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { Plus, Edit, Delete, Warning, Search, Refresh, Download, Document } from '@element-plus/icons-vue'
import ExportButtons from '@/components/ExportButtons.vue'
import { 
    getCertificatesService, 
    addCertificateService, 
    updateCertificateService, 
    deleteCertificateService,
} from '@/api/certificate'

// 证书列表
const certificates = ref([])
const loading = ref(false)
const tableKey = ref(0)
const ships = ref([])

// 分页和搜索
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ shipId: '', status: '' })

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
    certificateName: '',
    certificateNo: '',
    issuingAuthority: '',
    issueDate: '',
    expiryDate: '',
    attachmentUrl: '',
    remarks: ''
})

// 表单规则
const formRules = {
    shipId: [{ required: true, message: '请选择船舶', trigger: 'change' }],
    certificateName: [{ required: true, message: '请输入证书名称', trigger: 'blur' }]
}

// 证书类型选项
const certificateTypes = [
    '船舶国籍证书',
    '船舶检验证书',
    '船舶安全设备证书',
    '国际吨位证书',
    '国际载重线证书',
    '船舶最低安全配员证书',
    '船舶营运证书',
    '防止油污证书'
]

// 状态选项
const statusOptions = [
    { label: '有效', value: 'VALID' },
    { label: '即将到期', value: 'EXPIRING' },
    { label: '已过期', value: 'EXPIRED' }
]

// 状态标签类型
const statusTagMap = {
    'VALID': { type: 'success', label: '有效' },
    'EXPIRING': { type: 'warning', label: '即将到期' },
    'EXPIRED': { type: 'danger', label: '已过期' }
}

// 导出配置
const exportHeaders = [
    { key: 'id', title: 'ID', width: 60 },
    { key: 'shipId', title: '船舶ID', width: 80 },
    { key: 'certificateName', title: '证书名称', width: 150 },
    { key: 'certificateNo', title: '证书编号', width: 140 },
    { key: 'issuingAuthority', title: '发证机关', width: 140 },
    { key: 'issueDate', title: '发证日期', width: 110 },
    { key: 'expiryDate', title: '到期日期', width: 110 },
    { key: 'status', title: '状态', width: 100 },
    { key: 'remarks', title: '备注', width: 150 }
]

// 获取全部数据用于导出
const fetchAllCertificates = async () => {
    const res = await getCertificatesService({ pageNum: 1, pageSize: 1000 })
    return res.data?.records || []
}

// 获取证书列表
const fetchCertificates = async () => {
    loading.value = true
    try {
        const res = await getCertificatesService({
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize,
            shipId: searchForm.shipId || undefined,
            status: searchForm.status || undefined
        })
        await nextTick()
        certificates.value = res.data.records
        pagination.total = res.data.total
        tableKey.value++
    } catch (error) {
        ElMessage.error('获取证书列表失败')
    } finally {
        loading.value = false
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
    fetchCertificates()
}
const handleSearch = () => {
    pagination.pageNum = 1
    fetchCertificates()
}
const handleReset = () => {
    searchForm.shipId = ''
    searchForm.status = ''
    pagination.pageNum = 1
    fetchCertificates()
}

// 弹窗相关
const handleAdd = () => {
    isEdit.value = false
    dialogTitle.value = '新增证书'
    formData.value = {
        id: null,
        shipId: null,
        certificateName: '',
        certificateNo: '',
        issuingAuthority: '',
        issueDate: '',
        expiryDate: '',
        attachmentUrl: '',
        remarks: ''
    }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    isEdit.value = true
    dialogTitle.value = '编辑证书'
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
                    await updateCertificateService(formData.value)
                    ElMessage.success('更新成功')
                } else {
                    await addCertificateService(formData.value)
                    ElMessage.success('添加成功')
                }
                dialogVisible.value = false
                fetchCertificates()
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
        await ElMessageBox.confirm('确定要删除这个证书吗？', '提示', { type: 'warning' })
        await deleteCertificateService(row.id)
        ElMessage.success('删除成功')
        fetchCertificates()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败')
    }
}

const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleDateString('zh-CN')
}

const getShipName = (shipId) => {
    const ship = ships.value.find(s => s.id === shipId)
    return ship?.name || '-'
}

onMounted(() => {
    fetchCertificates()
    fetchShips()
})
</script>

<template>
    <div class="certificate-list-container">
        <div class="page-header">
            <div class="header-left">
                <h2 class="page-title">证书管理</h2>
                <p class="page-subtitle">管理船舶证书和到期提醒</p>
            </div>
            <div class="header-right">
                <el-button type="primary" :icon="Plus" @click="handleAdd">新增证书</el-button>
            </div>
        </div>

        <el-card shadow="never" class="list-card">
            <div class="search-bar">
                <el-select v-model="searchForm.shipId" placeholder="选择船舶" clearable filterable style="width: 200px">
                    <el-option v-for="ship in ships" :key="ship.id" :label="ship.name" :value="ship.id" />
                </el-select>
                <el-select v-model="searchForm.status" placeholder="证书状态" clearable style="width: 150px">
                    <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
                <div class="search-bar-right">
                    <ExportButtons
                        :currentData="certificates"
                        :fetchAllDataFn="fetchAllCertificates"
                        filename="船舶证书"
                        :headers="exportHeaders"
                        size="default"
                    />
                </div>
            </div>

            <el-table
                :data="certificates"
                :key="tableKey"
                v-loading="loading"
                stripe
                style="width: 100%"
            >
                <el-table-column label="证书信息" min-width="200">
                    <template #default="{ row }">
                        <div class="cert-info">
                            <el-icon class="cert-icon"><Document /></el-icon>
                            <div class="cert-text">
                                <div class="cert-name">{{ row.certificateName }}</div>
                                <div class="cert-no">{{ row.certificateNo }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="所属船舶" width="150">
                    <template #default="{ row }">
                        {{ getShipName(row.shipId) }}
                    </template>
                </el-table-column>
                <el-table-column prop="issuingAuthority" label="发证机关" width="180" show-overflow-tooltip />
                <el-table-column label="发证日期" width="120">
                    <template #default="{ row }">
                        {{ formatDate(row.issueDate) }}
                    </template>
                </el-table-column>
                <el-table-column label="到期日期" width="120">
                    <template #default="{ row }">
                        {{ formatDate(row.expiryDate) }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
                            <el-icon v-if="row.status === 'EXPIRING' || row.status === 'EXPIRED'" style="margin-right: 4px; vertical-align: middle;">
                                <Warning />
                            </el-icon>
                            {{ statusTagMap[row.status]?.label || row.status }}
                        </el-tag>
                    </template>
                </el-table-column>
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
                    @size-change="fetchCertificates"
                    @current-change="handlePageChange"
                    background
                />
            </div>

            <el-empty v-if="certificates.length === 0 && !loading" description="暂无证书数据">
                <el-button type="primary" @click="handleAdd">新增证书</el-button>
            </el-empty>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-form-item label="船舶" prop="shipId">
                    <el-select v-model="formData.shipId" placeholder="请选择船舶" filterable style="width: 100%">
                        <el-option v-for="ship in ships" :key="ship.id" :label="ship.name" :value="ship.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="证书名称" prop="certificateName">
                    <el-select v-model="formData.certificateName" filterable allow-create placeholder="请选择或输入证书名称" style="width: 100%">
                        <el-option v-for="type in certificateTypes" :key="type" :label="type" :value="type" />
                    </el-select>
                </el-form-item>
                <el-form-item label="证书编号">
                    <el-input v-model="formData.certificateNo" placeholder="请输入证书编号" />
                </el-form-item>
                <el-form-item label="发证机关">
                    <el-input v-model="formData.issuingAuthority" placeholder="请输入发证机关" />
                </el-form-item>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="发证日期">
                            <el-date-picker 
                                v-model="formData.issueDate" 
                                type="date" 
                                placeholder="选择日期"
                                value-format="YYYY-MM-DD"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="到期日期">
                            <el-date-picker 
                                v-model="formData.expiryDate" 
                                type="date" 
                                placeholder="选择日期"
                                value-format="YYYY-MM-DD"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="备注">
                    <el-input v-model="formData.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
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
.certificate-list-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.list-card { min-height: calc(100vh - 180px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.search-bar-right { margin-left: auto; display: flex; align-items: center; }

.cert-info { display: flex; align-items: center; gap: 12px; }
.cert-icon { font-size: 24px; color: var(--coral-600); background: rgba(255, 107, 107, 0.12); padding: 8px; border-radius: 10px; }
.cert-text { display: flex; flex-direction: column; }
.cert-name { font-weight: 600; color: var(--text-primary); }
.cert-no { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
