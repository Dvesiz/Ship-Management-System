/**
 * @file 操作日志 API 接口
 * @description 封装操作日志相关的后端接口调用(管理员功能)
 */

import request from '@/utils/request'

/**
 * 获取操作日志列表(分页)
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.username - 用户名(可选)
 * @param {string} params.module - 模块(可选)
 * @param {string} params.operation - 操作类型(可选)
 */
export const getLogsService = (params) => {
    return request.get('/log', { params })
}

/**
 * 清理指定天数前的日志
 * @param {number} days - 天数
 */
export const cleanLogsService = (days) => {
    return request.delete(`/log/clean/${days}`)
}
