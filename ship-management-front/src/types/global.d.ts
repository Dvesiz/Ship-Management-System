/**
 * @file 全局类型声明文件
 * @description 扩展 TypeScript 的全局类型定义，主要为 Cloudflare Turnstile 人机验证组件提供类型支持
 * @author Claude Code
 * @date 2024
 *
 * 该文件使用 `/// <reference>` 指令引用 Vite 客户端类型，
 * 并通过接口合并方式扩展 Window 对象，为第三方库提供类型声明
 */

/// <reference types="vite/client" />

/**
 * 扩展 Window 接口，为 Cloudflare Turnstile 提供类型支持
 * Turnstile 是 Cloudflare 提供的人机验证服务，用于区分真实用户和机器人
 */
interface Window {
  /**
   * Turnstile 全局对象（由 Cloudflare CDN 脚本加载）
   * 提供验证码组件的渲染和管理功能
   */
  turnstile?: {
    /**
     * 渲染 Turnstile Widget
     * @param {string | HTMLElement} container - 容器元素 ID 或 DOM 元素
     * @param {Object} options - 配置选项对象
     * @returns {string} 返回组件 ID
     */
    render: (
      container: string | HTMLElement,
      options: {
        /** Cloudflare 站点密钥，用于标识您的站点 */
        sitekey: string

        /** 验证成功回调，返回验证令牌 */
        callback: (token: string) => void

        /** 验证错误回调，用于处理验证失败情况 */
        'error-callback': () => void

        /** 验证过期回调，用于处理验证令牌过期情况 */
        'expired-callback': () => void
      }
    ) => string

    /**
     * 重置指定 Widget 的验证状态
     * 清除用户交互状态并允许重新验证
     * @param {string} widgetId - Widget 的唯一标识符
     */
    reset: (widgetId: string) => void

    /**
     * 移除指定 Widget
     * 从 DOM 中删除组件并清理相关资源
     * @param {string} widgetId - Widget 的唯一标识符
     */
    remove: (widgetId: string) => void

    /**
     * 手动执行验证
     * 触发验证流程，通常在自定义交互场景下使用
     * @param {string} [widgetId] - 可选的 Widget 标识符
     */
    execute: (widgetId?: string) => void
  }
}
