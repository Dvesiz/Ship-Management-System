/**
 * @file Token 状态管理 Store
 * @description 使用 Pinia 管理 Token 状态，提供设置、移除 Token 的方法，并同步到 localStorage
 * @author Claude Code
 * @date 2024
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Token 状态 Store
 * 使用 Composition API 风格定义
 */
export const useTokenStore = defineStore('token', () => {
    /**
     * Token 响应式变量
     * 优先从 localStorage 中读取，如果不存在则初始化为空字符串
     * @type {import('vue').Ref<string>}
     */
    const token = ref(localStorage.getItem('token') || '')

    /**
     * 设置 Token
     * 同时更新响应式状态和 localStorage
     * @param {string} newToken - 新的 Token 值
     */
    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    /**
     * 移除 Token
     * 同时清空响应式状态和 localStorage
     * 通常在用户退出登录时调用
     */
    const removeToken = () => {
        token.value = ''
        localStorage.removeItem('token')
    }

    // 导出状态和方法
    return {
        token,
        setToken,
        removeToken
    }
})
