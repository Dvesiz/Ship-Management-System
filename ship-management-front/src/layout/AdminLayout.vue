<script setup>
import {
  Ship, UserFilled, List, Tools, Files, Sunny, Sunset, HomeFilled,
  Document, DataAnalysis, Bell, Setting, Tickets, SwitchButton, Expand, Fold,
  ArrowRight, Location, ArrowDown
} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTokenStore } from '@/stores/token'
import { useThemeStore } from '@/stores/theme'
import { ElMessageBox, ElMessage } from 'element-plus'
import { computed, ref, onMounted } from 'vue'
import { getUnreadCountService } from '@/api/message'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tokenStore = useTokenStore()
const themeStore = useThemeStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCountService()
    unreadCount.value = res.data
  } catch (error) {
    console.error('获取未读消息数量失败', error)
  }
}

const isAdmin = computed(() => {
  return userStore.user.role === 'ADMIN'
})

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
    tokenStore.removeToken()
    userStore.clearUser()
    router.push('/login')
    ElMessage.success('退出成功')
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    title: item.meta.title,
    path: item.path
  }))
})

onMounted(() => {
  fetchUnreadCount()
  setInterval(fetchUnreadCount, 60000)
})
</script>

<template>
  <div class="layout-container" :class="{ 'is-collapsed': isCollapse }">
    <el-aside :width="isCollapse ? '72px' : '280px'" class="sidebar">
      <div class="logo-section">
        <div class="logo-icon-wrapper">
          <img src="@/assets/logo.jpg" alt="Logo" class="logo-image" />
        </div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text-group">
            <span class="logo-text">航运智控</span>
            <span class="logo-subtitle">智能船舶管理平台</span>
          </div>
        </transition>
      </div>

      <div v-if="!isCollapse" class="user-panel">
        <el-avatar
          :size="44"
          :src="userStore.user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
          class="user-panel-avatar"
        />
        <div class="user-panel-info">
          <div class="user-panel-name">{{ userStore.user.nickname || userStore.user.username }}</div>
          <div class="user-panel-role">{{ isAdmin ? '系统管理员' : '运营成员' }}</div>
        </div>
        <div class="user-panel-tag">在线</div>
      </div>

      <div v-if="!isCollapse" class="menu-group-title">核心导航</div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/ship">
          <el-icon><Ship /></el-icon>
          <template #title>船舶管理</template>
        </el-menu-item>

        <el-menu-item index="/category">
          <el-icon><Files /></el-icon>
          <template #title>船舶分类</template>
        </el-menu-item>

        <el-menu-item index="/crew">
          <el-icon><UserFilled /></el-icon>
          <template #title>船员管理</template>
        </el-menu-item>

        <el-menu-item index="/voyage">
          <el-icon><Location /></el-icon>
          <template #title>航行记录</template>
        </el-menu-item>

        <el-menu-item index="/maintenance">
          <el-icon><Tools /></el-icon>
          <template #title>维护记录</template>
        </el-menu-item>

        <el-menu-item index="/certificate">
          <el-icon><Document /></el-icon>
          <template #title>证书管理</template>
        </el-menu-item>

        <el-menu-item index="/fuel">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>燃油记录</template>
        </el-menu-item>

        <el-menu-item index="/message">
          <el-icon><Bell /></el-icon>
          <template #title>
            <span>消息中心</span>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="menu-badge" />
          </template>
        </el-menu-item>
      </el-menu>

      <div v-if="!isCollapse" class="menu-group-title">系统管理</div>
      <el-menu
        v-if="isAdmin"
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu secondary-menu"
      >
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/log">
          <el-icon><Tickets /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>

      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon :size="16">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
        <span v-show="!isCollapse" class="collapse-text">{{ isCollapse ? '' : '收起侧边栏' }}</span>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb :separator="'/'" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
              <span v-if="index === breadcrumbs.length - 1" class="breadcrumb-current">{{ item.title }}</span>
              <router-link v-else :to="item.path">{{ item.title }}</router-link>
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="status-stream">
            <span class="status-tag">运行稳定</span>
            <span class="status-text">今日告警 0</span>
            <span class="status-divider"></span>
            <span class="status-text">数据刷新中</span>
          </div>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <el-button
              circle
              :icon="themeStore.getCurrentTheme === 'light' ? Sunset : Sunny"
              @click="toggleTheme"
              class="theme-btn"
              title="切换主题"
            />
            <div class="header-chip">在线</div>
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-dropdown">
              <el-avatar
                :size="32"
                :src="userStore.user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                class="user-avatar"
              />
              <span class="username">{{ userStore.user.nickname || userStore.user.username }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="UserFilled">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  background: var(--bg-primary);
  background-image: radial-gradient(circle at 85% 0%, rgba(255, 107, 107, 0.08), transparent 45%);
  padding: 16px;
  position: relative;
}

/* 侧边栏容器 - 科技深色舱壁 */
.sidebar {
  background: var(--card-bg) !important;
  display: flex;
  flex-direction: column;
  transition: width var(--transition-slow);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: var(--shadow-lg);
  position: fixed;
  top: 16px;
  left: 16px;
  bottom: 16px;
  z-index: 200;
  border: var(--card-border);
  border-radius: var(--radius-xl);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

/* Logo 区域 */
.logo-section {
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 20px;
  flex-shrink: 0;
  background: transparent;
  margin-bottom: 8px;
  position: relative;
}

/* Logo 图标容器 */
.logo-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--coral-500);
  box-shadow: var(--shadow-md);
  transition: transform var(--transition-normal);
}

.logo-section:hover .logo-icon-wrapper {
  transform: rotate(2deg) scale(1.03);
}

.logo-image {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.logo-text-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logo-text {
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.6px;
  font-family: var(--font-display);
}

.logo-subtitle {
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.4px;
}

.user-panel {
  margin: 8px 16px 16px;
  padding: 12px 14px;
  background: var(--card-bg);
  border: var(--card-border);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.user-panel-avatar {
  border: 2px solid rgba(255, 107, 107, 0.35);
}

.user-panel-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-panel-name {
  font-size: 14px;
  font-weight: 600;
}

.user-panel-role {
  font-size: 12px;
  color: var(--text-tertiary);
}

.user-panel-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 107, 107, 0.14);
  color: var(--coral-600);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.menu-group-title {
  padding: 8px 20px 6px;
  font-size: 12px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 菜单区域 */
.sidebar-menu {
  flex: 1;
  background: transparent !important;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 12px;
  border-right: none;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.sidebar-menu.secondary-menu {
  flex: none;
  padding-bottom: 8px;
}

.sidebar-menu::-webkit-scrollbar {
  display: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

/* 菜单项基础样式 */
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 46px;
  line-height: 46px;
  margin: 6px 0;
  border-radius: var(--radius-md);
  color: var(--text-secondary) !important;
  font-size: 14px;
  background: transparent !important;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
  display: flex;
  align-items: center;
}

/* 图标样式 */
.sidebar-menu :deep(.el-menu-item .el-icon),
.sidebar-menu :deep(.el-sub-menu__title .el-icon) {
  color: var(--text-tertiary) !important;
  margin-right: 12px;
  font-size: 18px;
  transition: all var(--transition-fast);
}

/* 悬停状态 */
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 107, 107, 0.12) !important;
  color: var(--text-primary) !important;
  border-color: rgba(255, 107, 107, 0.2);
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon),
.sidebar-menu :deep(.el-sub-menu__title:hover .el-icon) {
  color: var(--coral-500) !important;
}

/* 选中状态 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 107, 107, 0.16) !important;
  color: var(--text-primary) !important;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(255, 107, 107, 0.22), var(--shadow-sm);
}

.menu-badge {
  margin-left: 6px;
}

/* 选中状态的图标 */
.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--coral-500) !important;
  font-weight: 600;
}

.sidebar-menu :deep(.el-sub-menu__title) {
  padding-left: 16px !important;
}
.sidebar-menu :deep(.el-menu-item) {
  padding-left: 16px !important;
}

/* 折叠时居中 */
.el-menu--collapse :deep(.el-menu-item),
.el-menu--collapse :deep(.el-sub-menu__title) {
  padding: 0 !important;
  justify-content: center !important;
  margin: 8px auto !important;
  width: 46px !important;
}

/* 折叠时取消图标边距 */
.el-menu--collapse :deep(.el-menu-item .el-icon),
.el-menu--collapse :deep(.el-sub-menu__title .el-icon) {
  margin: 0 !important;
  font-size: 20px !important;
  text-align: center;
  width: auto;
}

/* 折叠时隐藏箭头 */
.el-menu--collapse :deep(.el-sub-menu__icon-arrow) {
  display: none !important;
}

/* 折叠时的 Tooltip 修饰 */
.el-menu--collapse :deep(.el-menu-item:hover),
.el-menu--collapse :deep(.el-sub-menu__title:hover) {
  transform: scale(1.05) !important;
  background: rgba(255, 107, 107, 0.2) !important;
}

/* 折叠按钮 */
.collapse-btn {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px 20px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: var(--card-border);
  color: var(--text-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.collapse-btn:hover {
  background: rgba(255, 107, 107, 0.14);
  border-color: rgba(255, 107, 107, 0.3);
  color: var(--text-primary);
}

.collapse-text {
  font-size: 13px;
  margin-left: 8px;
  font-weight: 600;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: transparent;
  margin-left: calc(280px + 16px);
  border-radius: var(--radius-xl);
}

.layout-container.is-collapsed .main-container {
  margin-left: calc(72px + 16px);
}

.header {
  background: var(--card-bg);
  border: var(--card-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 58px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
  z-index: 100;
  border-radius: var(--radius-lg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  margin-bottom: 16px;
}

.header-left {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-stream {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.status-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 107, 107, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.3);
  color: var(--coral-600);
  font-weight: 600;
}

.status-text {
  letter-spacing: 0.4px;
}

.status-divider {
  width: 16px;
  height: 1px;
  background: var(--border-color);
}

.breadcrumb {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
  transition: color 0.2s;
}

.breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--text-primary);
}

.breadcrumb-current {
  color: var(--text-primary);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-radius: 999px;
  background: var(--card-bg);
  border: var(--card-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  box-shadow: var(--shadow-sm);
}

.header-chip {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(255, 107, 107, 0.14);
  color: var(--coral-600);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.theme-btn {
  border: 1px solid rgba(255, 107, 107, 0.25);
  background: rgba(255, 107, 107, 0.14);
  color: var(--coral-700);
  width: 36px;
  height: 36px;
  border-radius: 12px;
  transition: all var(--transition-fast);
}

.theme-btn:hover {
  background: rgba(255, 107, 107, 0.22);
  color: var(--coral-800);
  transform: translateY(-1px);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 14px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  background: var(--card-bg);
  border: var(--card-border);
}

.user-dropdown:hover {
  background: rgba(255, 107, 107, 0.12);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.user-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(255, 107, 107, 0.35);
}

.username {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  color: var(--text-tertiary);
  font-size: 12px;
  margin-left: 2px;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 20px 28px;
  background: transparent;
  position: relative;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .main-container,
  .layout-container.is-collapsed .main-container {
    margin-left: 0;
  }

  .username {
    display: none;
  }
  
  .collapse-text {
    display: none;
  }
  
  .header {
    padding: 0 16px;
  }
  
  .main-content {
    padding: 14px 12px 20px;
  }
}

</style>
