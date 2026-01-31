/**
 * @file HTTP 请求封装
 * @description 基于 axios 封装 HTTP 请求，提供请求/响应拦截器，统一处理 Token 认证和错误处理
 * @author Claude Code
 * @date 2024
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建需要认证的 axios 实例
 * 用于需要登录后才能访问的接口
 */
const service = axios.create({
  baseURL: '/api',              // 接口基础路径
  timeout: 15000000             // 请求超时时间（毫秒）
})

/**
 * 创建无需认证的 axios 实例
 * 用于登录、注册、重置密码等公开接口
 */
const publicService = axios.create({
  baseURL: '/api',
  timeout: 15000000
})

/**
 * 请求拦截器
 * 在每个请求发送前自动添加 Token 到请求头
 */
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 设置 Authorization 请求头
      config.headers['Authorization'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

/**
 * 响应拦截器
 * 统一处理响应数据和错误
 */
service.interceptors.response.use(
  response => {
    const res = response.data
    // 业务状态码不为 0，表示业务错误
    if (res.code !== 0) {
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    // HTTP 状态码 401：未授权（Token 过期）
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      location.reload()
    } else {
      // 其他错误
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 导出 axios 实例
export default service
export { publicService }
