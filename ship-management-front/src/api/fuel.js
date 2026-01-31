/**
 * @file 燃油记录 API 接口
 * @description 封装燃油记录相关的后端接口调用
 */

import request from '@/utils/request'

/**
 * 获取燃油记录列表(分页)
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.shipId - 船舶ID(可选)
 * @param {number} params.voyageId - 航次ID(可选)
 * @param {string} params.fuelType - 燃油类型(可选)
 */
export const getFuelRecordsService = (params) => {
    return request.get('/fuel', { params })
}

/**
 * 获取燃油记录详情
 * @param {number} id - 记录ID
 */
export const getFuelRecordByIdService = (id) => {
    return request.get(`/fuel/${id}`)
}

/**
 * 添加燃油记录
 * @param {Object} data - 燃油记录数据
 */
export const addFuelRecordService = (data) => {
    return request.post('/fuel', data)
}

/**
 * 更新燃油记录
 * @param {Object} data - 燃油记录数据
 */
export const updateFuelRecordService = (data) => {
    return request.put('/fuel', data)
}

/**
 * 删除燃油记录
 * @param {number} id - 记录ID
 */
export const deleteFuelRecordService = (id) => {
    return request.delete(`/fuel/${id}`)
}

/**
 * 统计船舶燃油总消耗
 * @param {number} shipId - 船舶ID
 */
export const getFuelQuantityStatsService = (shipId) => {
    return request.get(`/fuel/stats/quantity/${shipId}`)
}

/**
 * 统计船舶燃油总费用
 * @param {number} shipId - 船舶ID
 */
export const getFuelCostStatsService = (shipId) => {
    return request.get(`/fuel/stats/cost/${shipId}`)
}

/**
 * 按月统计燃油消耗
 * @param {number} shipId - 船舶ID
 */
export const getMonthlyFuelStatsService = (shipId) => {
    return request.get(`/fuel/stats/monthly/${shipId}`)
}

/**
 * 获取燃油消耗统计概览
 */
export const getFuelOverviewService = () => {
    return request.get('/fuel/stats/overview')
}
