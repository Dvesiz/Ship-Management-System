/**
 * @file 消息通知 API 接口
 * @description 封装消息通知相关的后端接口调用
 */

import request from '@/utils/request'

/**
 * 获取消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.type - 消息类型(可选)
 * @param {string} params.status - 消息状态(可选)
 */
export const getMessagesService = (params) => {
    return request.get('/message', { params })
}

/**
 * 获取未读消息数量
 */
export const getUnreadCountService = () => {
    return request.get('/message/unread-count')
}

/**
 * 标记消息为已读
 * @param {number} messageId - 消息ID
 */
export const markAsReadService = (messageId) => {
    return request.put(`/message/read/${messageId}`)
}

/**
 * 标记所有消息为已读
 */
export const markAllAsReadService = () => {
    return request.put('/message/read-all')
}

/**
 * 删除消息
 * @param {number} messageId - 消息ID
 */
export const deleteMessageService = (messageId) => {
    return request.delete(`/message/${messageId}`)
}

/**
 * 发送站内信
 * @param {Object} data - 消息数据
 * @param {number} data.receiverId - 接收者ID
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export const sendMessageService = (data) => {
    const params = new URLSearchParams()
    params.append('receiverId', data.receiverId)
    params.append('title', data.title)
    params.append('content', data.content)
    if (data.type) {
        params.append('type', data.type)
    }
    return request.post('/message/send', params)
}
