/**
 * @file 船舶证书 API 接口
 * @description 封装船舶证书相关的后端接口调用
 */

import request from '@/utils/request'

/**
 * 获取证书列表(分页)
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.shipId - 船舶ID(可选)
 * @param {string} params.status - 证书状态(可选)
 */
export const getCertificatesService = (params) => {
    return request.get('/certificate', { params })
}

/**
 * 获取证书详情
 * @param {number} id - 证书ID
 */
export const getCertificateByIdService = (id) => {
    return request.get(`/certificate/${id}`)
}

/**
 * 获取船舶的所有证书
 * @param {number} shipId - 船舶ID
 */
export const getCertificatesByShipService = (shipId) => {
    return request.get(`/certificate/ship/${shipId}`)
}

/**
 * 添加证书
 * @param {Object} data - 证书数据
 */
export const addCertificateService = (data) => {
    return request.post('/certificate', data)
}

/**
 * 更新证书
 * @param {Object} data - 证书数据
 */
export const updateCertificateService = (data) => {
    return request.put('/certificate', data)
}

/**
 * 删除证书
 * @param {number} id - 证书ID
 */
export const deleteCertificateService = (id) => {
    return request.delete(`/certificate/${id}`)
}

/**
 * 获取即将到期的证书
 */
export const getExpiringCertificatesService = () => {
    return request.get('/certificate/expiring')
}

/**
 * 获取已过期的证书
 */
export const getExpiredCertificatesService = () => {
    return request.get('/certificate/expired')
}
