/**
 * @file 路由配置文件
 * @description 定义应用的所有路由规则，包括登录页、重置密码页和管理后台路由，并配置路由守卫实现权限控制
 * @author Claude Code
 * @date 2024
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '../layout/AdminLayout.vue'
import Login from '../Login.vue'

/**
 * 路由配置数组
 * 定义了应用的所有页面路径和对应的组件
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }                      // 页面元信息，用于设置文档标题
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/user/ResetPassword.vue'),  // 路由懒加载
    meta: { title: '重置密码' }
  },
  {
    path: '/',
    component: AdminLayout,                        // 使用管理后台布局组件
    redirect: '/dashboard',                       // 默认重定向到首页
    children: [                                  // 嵌套子路由
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'ship',
        name: 'Ship',
        component: () => import('../views/ship/ShipList.vue'),
        meta: { title: '船舶管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/category/CategoryList.vue'),
        meta: { title: '船舶类型' }
      },
      {
        path: 'crew',
        name: 'Crew',
        component: () => import('../views/crew/CrewList.vue'),
        meta: { title: '船员管理' }
      },
      {
        path: 'voyage',
        name: 'Voyage',
        component: () => import('../views/voyage/VoyageList.vue'),
        meta: { title: '航次记录' }
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('../views/maintenance/MaintenanceList.vue'),
        meta: { title: '维修保养' }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/user/UserProfile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'certificate',
        name: 'Certificate',
        component: () => import('../views/certificate/CertificateList.vue'),
        meta: { title: '证书管理' }
      },
      {
        path: 'fuel',
        name: 'Fuel',
        component: () => import('../views/fuel/FuelRecordList.vue'),
        meta: { title: '燃油记录' }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('../views/log/OperationLog.vue'),
        meta: { title: '操作日志', requireAdmin: true }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理', requireAdmin: true }
      }
    ]
  }
]

/**
 * 创建路由实例
 * 使用 HTML5 History 模式，URL 更美观
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由前置守卫
 * @param {Object} to - 即将进入的目标路由对象
 * @param {Object} from - 即将离开的起始路由对象
 * @param {Function} next - 路由跳转控制函数
 */
router.beforeEach(async (to, from, next) => {
  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')
  const userStore = useUserStore()

  // 定义无需登录即可访问的公开路径
  const publicPaths = ['/login', '/reset-password']

  // 场景 1: 没有 token 且访问受保护页面 -> 强制跳转到登录页
  if (!token && !publicPaths.includes(to.path)) {
    return next('/login')
  }

  // 场景 2: 有 token 但用户信息未加载 -> 重新获取用户信息
  if (token && !userStore.user.username) {
    try {
      await userStore.getUser()                  // 调用后端接口获取用户信息
    } catch (e) {
      // Token 可能已过期，清除并跳转登录
      localStorage.removeItem('token')
      return next('/login')
    }
  }

  // 场景 3: 检查管理员权限
  if (to.meta.requireAdmin && userStore.user.role !== 'ADMIN') {
    return next('/dashboard')  // 非管理员跳转到首页
  }

  // 场景 4: 验证通过，允许继续跳转
  next()
})

export default router
