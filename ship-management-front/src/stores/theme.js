/**
 * @file 主题状态管理 Store
 * @description 使用 Pinia 管理应用主题（亮色/暗色）状态，支持主题切换和持久化存储
 * @author Claude Code
 * @date 2024
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 主题状态 Store
 * 使用 Composition API 风格定义
 */
export const useThemeStore = defineStore('theme', () => {
  /**
   * 主题状态
   * @type {import('vue').Ref<'light' | 'dark'>}
   */
  const theme = ref('light')

  /**
   * 切换主题
   * 在亮色和暗色主题之间切换，并更新 DOM 属性和 localStorage
   */
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme.value)
    localStorage.setItem('theme', theme.value)
  }

  /**
   * 获取当前主题的只读计算属性
   * @returns {string} 当前主题值
   */
  const getCurrentTheme = computed(() => theme.value)

  /**
   * 初始化主题
   * 从 localStorage 读取保存的主题偏好，如果没有则使用默认亮色主题
   */
  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      theme.value = savedTheme
    } else {
      // 默认使用亮色主题，不检测系统主题
      theme.value = 'light'
    }
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // 导出状态和方法
  return {
    theme,
    toggleTheme,
    getCurrentTheme,
    initTheme
  }
})
