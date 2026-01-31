/**
 * @file 用户状态管理 Store
 * @description 使用 Pinia 管理用户信息状态，提供获取用户信息和清空用户信息的方法
 * @author Claude Code
 * @date 2024
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userInfoService } from '@/api/user'

/**
 * 用户状态 Store
 * 使用 Composition API 风格定义
 */
export const useUserStore = defineStore('user', () => {
    /**
     * 用户信息响应式对象
     * @type {import('vue').Ref<Object>}
     */
    const user = ref({})

    /**
     * 调用接口获取当前登录用户信息
     * @returns {Promise<void>}
     */
    const getUser = async () => {
        const res = await userInfoService()
        user.value = res.data
    }

    /**
     * 清空用户信息
     * 通常在用户退出登录时调用
     */
    const clearUser = () => {
        user.value = {}
    }

    // 导出状态和方法
    return {
        user,
        getUser,
        clearUser
    }
})
