/**
 * @file 应用入口文件
 * @description 负责初始化 Vue 应用实例，配置全局插件、Pinia 状态管理、路由和 Element Plus 组件库
 * @author Claude Code
 * @date 2024
 */

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import './style.css'
import './styles/button-fix.css'
import './styles/layout-fix.css'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'

/**
 * 创建 Vue 应用实例
 */
const app = createApp(App)

/**
 * 创建 Pinia 状态管理实例
 */
const pinia = createPinia()

/**
 * 全局注册所有 Element Plus 图标组件
 * 遍历 @element-plus/icons-vue 导出的所有图标组件并注册到全局
 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

/**
 * 注册插件到应用实例
 */
app.use(pinia)                           // 注册 Pinia 状态管理
app.use(router)                          // 注册 Vue Router 路由
app.use(ElementPlus, {                    // 注册 Element Plus UI 组件库
  locale: zhCn,                         // 设置中文语言包
})

/**
 * 初始化主题设置
 * 从 localStorage 读取用户保存的主题偏好并应用
 */
const themeStore = useThemeStore()
themeStore.initTheme()

/**
 * 挂载应用到 DOM
 */
app.mount('#app')
