/**
 * @file 管理员 API 接口
 * @description 封装管理员相关的后端接口调用
 */

import request from '@/utils/request'

/**
 * 获取用户列表(分页)
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.username - 用户名(可选)
 * @param {string} params.role - 角色(可选)
 */
export const getUsersService = (params) => {
    return request.get('/admin/user', { params })
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 */
export const getUserByIdService = (id) => {
    return request.get(`/admin/user/${id}`)
}

/**
 * 更新用户角色
 * @param {number} userId - 用户ID
 * @param {string} role - 角色
 */
export const updateUserRoleService = (userId, role) => {
    const params = new URLSearchParams()
    params.append('userId', userId)
    params.append('role', role)
    return request.put('/admin/user/role', params)
}

/**
 * 重置用户密码
 * @param {number} userId - 用户ID
 * @param {string} newPassword - 新密码
 */
export const resetUserPasswordService = (userId, newPassword) => {
    const params = new URLSearchParams()
    params.append('userId', userId)
    params.append('newPassword', newPassword)
    return request.put('/admin/user/reset-password', params)
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 */
export const deleteUserService = (id) => {
    return request.delete(`/admin/user/${id}`)
}

/**
 * 获取用户统计信息
 */
export const getUserStatsService = () => {
    return request.get('/admin/user/stats')
}
