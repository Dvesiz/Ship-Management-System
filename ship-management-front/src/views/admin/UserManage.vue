<script setup>
/**
 * @file 用户管理页面(管理员功能)
 */
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersService, updateUserRoleService, resetUserPasswordService, deleteUserService, getUserStatsService } from '@/api/admin'
import { Search, Refresh, Delete, Key, User, VideoPlay } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const tableKey = ref(0)
const total = ref(0)
const stats = ref({ totalUsers: 0, adminCount: 0, userCount: 0 })
const pageNum = ref(1)
const pageSize = ref(10)
const filterUsername = ref('')
const filterRole = ref('')
const roleOptions = [
    { label: '管理员', value: 'ADMIN' },
    { label: '普通用户', value: 'USER' }
]

const fetchUsers = async () => {
    loading.value = true
    try {
        const res = await getUsersService({ 
            pageNum: pageNum.value, 
            pageSize: pageSize.value, 
            username: filterUsername.value || undefined, 
            role: filterRole.value || undefined 
        })
        await nextTick()
        users.value = res.data.records
        total.value = res.data.total
        tableKey.value++
    } catch (e) { 
        ElMessage.error('获取用户列表失败') 
    } finally { 
        loading.value = false 
    }
}

const fetchStats = async () => { 
    try { 
        const res = await getUserStatsService()
        stats.value = res.data 
    } catch (e) { 
        console.error(e) 
    } 
}

const handleSearch = () => { pageNum.value = 1; fetchUsers() }
const handleReset = () => { filterUsername.value = ''; filterRole.value = ''; pageNum.value = 1; fetchUsers() }
const handlePageChange = (page) => { pageNum.value = page; fetchUsers() }
const handleSizeChange = (size) => { pageSize.value = size; pageNum.value = 1; fetchUsers() }

const handleRoleChange = async (user, role) => {
    try { 
        await updateUserRoleService(user.id, role)
        ElMessage.success('角色更新成功')
        fetchUsers()
        fetchStats() 
    } catch (e) { 
        ElMessage.error('更新失败')
        fetchUsers() // Revert UI
    }
}

const handleResetPassword = async (user) => {
    try {
        const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', { 
            inputPattern: /^\S{5,16}$/, 
            inputErrorMessage: '密码长度5-16位',
            confirmButtonText: '确定',
            cancelButtonText: '取消'
        })
        await resetUserPasswordService(user.id, value)
        ElMessage.success('密码重置成功')
    } catch (e) { 
        if (e !== 'cancel') ElMessage.error('重置失败') 
    }
}

const handleDelete = async (user) => {
    try {
        await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
        await deleteUserService(user.id)
        ElMessage.success('删除成功')
        if (users.value.length === 1 && pageNum.value > 1) {
            pageNum.value--
        }
        fetchUsers()
        fetchStats()
    } catch (e) { 
        if (e !== 'cancel') ElMessage.error('删除失败') 
    }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

onMounted(() => { fetchUsers(); fetchStats() })
</script>

<template>
    <div class="user-manage-container">
        <div class="page-header">
            <div class="header-left">
                <h2 class="page-title">用户管理</h2>
                <p class="page-subtitle">管理系统用户及权限</p>
            </div>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-blue">
                            <el-icon><User /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.totalUsers }}</div>
                            <div class="stat-label">用户总数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-orange">
                            <el-icon><User /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.adminCount }}</div>
                            <div class="stat-label">管理员</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="never" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-icon bg-green">
                            <el-icon><User /></el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.userCount }}</div>
                            <div class="stat-label">普通用户</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-card shadow="never" class="list-card">
            <div class="search-bar">
                <el-input 
                    v-model="filterUsername" 
                    placeholder="搜索用户名" 
                    clearable 
                    prefix-icon="Search"
                    style="width: 200px" 
                    @keyup.enter="handleSearch"
                />
                <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width: 150px">
                    <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            </div>

            <el-table 
                :data="users" 
                :key="tableKey"
                v-loading="loading" 
                stripe 
                style="width: 100%"
            >
                <el-table-column prop="username" label="用户名" min-width="120">
                    <template #default="{ row }">
                        <div class="user-cell">
                            <el-avatar :size="32" class="user-avatar">{{ row.username.charAt(0).toUpperCase() }}</el-avatar>
                            <span>{{ row.username }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="nickname" label="昵称" width="120" />
                <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
                <el-table-column label="角色" width="150" align="center">
                    <template #default="{ row }">
                        <el-select 
                            v-model="row.role" 
                            @change="handleRoleChange(row, row.role)" 
                            size="small"
                            :class="['role-select', row.role === 'ADMIN' ? 'role-admin' : 'role-user']"
                        >
                            <el-option label="管理员" value="ADMIN" />
                            <el-option label="普通用户" value="USER" />
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="注册时间" width="170" align="center">
                    <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center" fixed="right">
                    <template #default="{ row }">
                        <div class="action-buttons">
                            <el-button type="warning" link :icon="Key" @click="handleResetPassword(row)">重置密码</el-button>
                            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-wrapper" v-if="total > 0">
                <el-pagination
                    v-model:current-page="pageNum"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @current-change="handlePageChange"
                    @size-change="handleSizeChange"
                    background
                />
            </div>
        </el-card>
    </div>
</template>

<style scoped>
.user-manage-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.stats-row { margin-bottom: 24px; }
.stat-card { border: 1px solid var(--card-border) !important; background: var(--card-bg) !important; box-shadow: var(--shadow-sm) !important; transition: transform 0.3s, box-shadow 0.3s; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
.stat-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md) !important; }

.stat-content { display: flex; align-items: center; gap: 16px; padding: 10px 0; }
.stat-icon { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; }

.bg-blue { background: var(--coral-500); box-shadow: var(--shadow-sm); }
.bg-orange { background: var(--warning); box-shadow: var(--shadow-sm); }
.bg-green { background: var(--success); box-shadow: var(--shadow-sm); }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }

.list-card { min-height: calc(100vh - 300px); background: var(--card-bg) !important; border: 1px solid var(--card-border) !important; box-shadow: var(--shadow-sm) !important; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.search-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 16px 20px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); margin-bottom: 20px; backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar { background: var(--coral-500); color: white; font-size: 14px; font-weight: 600; }

.role-select { width: 110px; }
.action-buttons { display: flex; justify-content: center; gap: 8px; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; padding: 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); backdrop-filter: var(--glass-blur); -webkit-backdrop-filter: var(--glass-blur); }
</style>
