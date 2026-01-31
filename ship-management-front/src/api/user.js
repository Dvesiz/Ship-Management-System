/**
 * @file 用户相关 API 接口
 * @description 封装所有用户相关的后端接口调用，包括登录、注册、发送验证码、获取用户信息等
 * @author Dvesiz
 * @date 2025
 */

import request, { publicService } from '@/utils/request'

/**
 * 获取图形验证码（已废弃）
 * @description 保留接口定义以防后端需要，当前版本不再使用
 * @deprecated
 */
// export const getCaptchaService = () => {
//     return request.get('/user/captcha')
// }

/**
 * 发送邮箱验证码
 * @description 向指定邮箱发送验证码
 * @param {string} email - 邮箱地址
 * @returns {Promise<AxiosResponse>} 返回发送结果
 */
export const sendCodeService = (email) => {
    const params = new URLSearchParams()
    params.append('email', email)
    return publicService.post('/user/send-code', params)
}

/**
 * 用户注册
 * @description 使用用户名、密码、邮箱和验证码进行用户注册
 * @param {Object} registerData - 注册数据
 * @param {string} registerData.username - 用户名
 * @param {string} registerData.password - 密码
 * @param {string} registerData.email - 邮箱地址
 * @param {string} registerData.code - 邮箱验证码
 * @returns {Promise<AxiosResponse>} 返回注册结果
 */
export const userRegisterService = (registerData) => {
    const params = new URLSearchParams()
    params.append('username', registerData.username)
    params.append('password', registerData.password)
    params.append('email', registerData.email)
    params.append('code', registerData.code)
    return request.post('/user/register', params)
}

/**
 * 账号密码登录
 * @description 使用用户名和密码进行登录，需要提供人机验证 token
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.username - 用户名
 * @param {string} loginData.password - 密码
 * @param {string} loginData.turnstileToken - Cloudflare Turnstile 验证 token
 * @returns {Promise<AxiosResponse>} 返回 Token
 */
export const userLoginService = (loginData) => {
    const params = new URLSearchParams()
    for (let key in loginData) {
        params.append(key, loginData[key])
    }
    return request.post('/user/login', params)
}

/**
 * 邮箱验证码登录
 * @description 使用邮箱和验证码进行登录，需要提供人机验证 token
 * @param {Object} data - 登录数据
 * @param {string} data.email - 邮箱地址
 * @param {string} data.code - 邮箱验证码
 * @param {string} data.turnstileToken - Cloudflare Turnstile 验证 token（可选）
 * @returns {Promise<AxiosResponse>} 返回 Token
 */
export const userLoginByEmailService = (data) => {
    const params = new URLSearchParams()
    params.append('email', data.email)
    params.append('code', data.code)
    if (data.turnstileToken) {
        params.append('turnstileToken', data.turnstileToken)
    }
    return request.post('/user/loginByEmail', params)
}

/**
 * 获取当前登录用户信息
 * @description 获取已登录用户的详细信息
 * @returns {Promise<AxiosResponse>} 返回用户信息对象
 */
export const userInfoService = () => {
    return request.get('/user/info')
}

/**
 * 邮箱验证码重置密码
 * @description 通过邮箱和验证码重置用户密码，无需登录
 * @param {Object} resetData - 重置数据
 * @param {string} resetData.email - 邮箱地址
 * @param {string} resetData.code - 邮箱验证码
 * @param {string} resetData.newPassword - 新密码
 * @returns {Promise<AxiosResponse>} 返回重置结果
 */
export const resetPasswordByEmailService = (resetData) => {
    const params = new URLSearchParams()
    params.append('email', resetData.email)
    params.append('code', resetData.code)
    params.append('newPassword', resetData.newPassword)

    // 重置密码不需要 token，使用 publicService 请求
    return publicService.post('/user/reset-password', params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

/**
 * 获取用户列表（管理员）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.role - 角色筛选
 * @returns {Promise<AxiosResponse>} 返回用户列表
 */
export const getUserListService = (params) => {
    return request.get('/admin/user', { params })
}

/**
 * 搜索用户（普通用户）
 * @param {string} query - 搜索关键词
 * @returns {Promise<AxiosResponse>} 返回用户列表
 */
export const searchUserService = (query) => {
    return request.get('/user/search', { params: { query } })
}
