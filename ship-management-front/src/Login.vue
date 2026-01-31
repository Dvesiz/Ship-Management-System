<script setup>
/**
 * @file 登录/注册页面组件
 * @description 提供用户注册和登录功能，支持账号密码登录和邮箱验证码登录，集成 Cloudflare Turnstile 人机验证
 * @author Dvesiz
 * @date 2025
 */

import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { User, Lock, Message, Key, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'
import logo from '@/assets/logo.jpg'

// 导入用户相关 API 服务
import {
  sendCodeService,
  userRegisterService,
  userLoginService,
  userLoginByEmailService
} from '@/api/user'

// 获取路由和 Store 实例
const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()

// 页面状态
const isRegister = ref(false)           // 是否显示注册模式
const loginType = ref('account')        // 登录方式：'account' 或 'email'
const timer = ref(0)                   // 验证码倒计时
const sendingCode = ref(false)          // 发送验证码加载状态
const submitting = ref(false)            // 提交表单加载状态
const turnstileWidgetId = ref(null)      // Turnstile 组件 ID
const turnstileToken = ref('')            // Turnstile 验证 Token

// 表单数据
const form = reactive({
  username: '',          // 用户名
  password: '',          // 密码
  confirmPassword: '',   // 确认密码
  email: '',             // 邮箱地址
  code: ''               // 邮箱验证码
})

/**
 * 加载 Turnstile 脚本
 * 动态加载 Cloudflare Turnstile 人机验证组件
 * @returns {Promise<void>}
 */
const loadTurnstileScript = () => {
  return new Promise((resolve, reject) => {
    // 如果已加载则直接返回
    if (window.turnstile) {
      resolve()
      return
    }

    // 创建 script 元素并动态加载
    const script = document.createElement('script')
    script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js'
    script.async = true
    script.defer = true
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

/**
 * 渲染 Turnstile Widget
 * 在页面挂载后初始化人机验证组件
 */
const renderTurnstile = () => {
  if (!window.turnstile || turnstileWidgetId.value) return

  const container = document.getElementById('turnstile-container')
  if (!container) return

  // 清空容器防止重复渲染
  container.innerHTML = ''

  // 调用 Turnstile API 渲染组件
  turnstileWidgetId.value = window.turnstile.render('#turnstile-container', {
    sitekey: '0x4AAAAAACJSGMZiJJhVnxve',     // 站点密钥
    theme: 'light',                            // 主题：亮色
    callback: (token) => {                        // 验证成功回调
      turnstileToken.value = token
    },
    'error-callback': () => {                     // 验证错误回调
      ElMessage.error('人机验证失败，请刷新重试')
      turnstileToken.value = ''
    },
    'expired-callback': () => {                   // 验证过期回调
      turnstileToken.value = ''
      ElMessage.warning('验证已过期，请重新验证')
    }
  })
}

/**
 * 重置 Turnstile 验证
 * 清除验证状态并重新渲染组件
 */
const resetTurnstile = () => {
  if (turnstileWidgetId.value !== null && window.turnstile) {
    window.turnstile.reset(turnstileWidgetId.value)
    turnstileToken.value = ''
  }
}

/**
 * 组件挂载后初始化 Turnstile
 */
onMounted(async () => {
  try {
    await loadTurnstileScript()
    await nextTick()
    renderTurnstile()
  } catch (error) {
    console.error('Failed to load Turnstile:', error)
    ElMessage.error('人机验证组件加载失败')
  }
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_, v, cb) => v === form.password ? cb() : cb(new Error('两次密码不一致')),
      trigger: 'blur'
    }
  ],
  email: [{ required: true, type: 'email', message: '邮箱格式错误', trigger: 'blur' }],
  code: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
}

// 表单引用
const formRef = ref()

// 验证码倒计时定时器引用
let codeInterval = null

/**
 * 发送邮箱验证码
 * 先验证邮箱格式，再调用后端接口发送验证码，启动 60 秒倒计时
 */
const sendEmailCode = async () => {
  try {
    await formRef.value.validateField('email')
  } catch {
    return
  }
  sendingCode.value = true
  try {
    await sendCodeService(form.email)
    ElMessage.success('验证码已发送')
    timer.value = 60
    // 清除旧的定时器
    if (codeInterval) {
      clearInterval(codeInterval)
      codeInterval = null
    }
    // 启动倒计时
    codeInterval = setInterval(() => {
      timer.value--
      if (timer.value <= 0) {
        clearInterval(codeInterval)
        codeInterval = null
        sendingCode.value = false
      }
    }, 1000)
  } catch (e) {
    ElMessage.error('发送验证码失败，请稍后重试')
    sendingCode.value = false
  }
}

/**
 * 处理表单提交
 * 根据当前模式执行注册或登录操作
 */
const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  try {
    let token = ''

    // 注册流程
    if (isRegister.value) {
      // 注册不需要人机验证
      await userRegisterService({ ...form })
      ElMessage.success('注册成功')
      isRegister.value = false
      formRef.value.resetFields()
      return
    }

    // 登录需要检查 Turnstile Token
    if (!turnstileToken.value) {
      ElMessage.warning('请先完成人机验证')
      submitting.value = false
      return
    }

    // 账号密码登录
    if (loginType.value === 'account') {
      const res = await userLoginService({
        username: form.username,
        password: form.password,
        turnstileToken: turnstileToken.value
      })
      token = res.data
    } else {
      // 邮箱验证码登录
      const res = await userLoginByEmailService({
        email: form.email,
        code: form.code,
        turnstileToken: turnstileToken.value
      })
      token = res.data
    }

    // 保存 Token 并获取用户信息
    tokenStore.setToken(token)
    await userStore.getUser()
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    submitting.value = false
    resetTurnstile()
  }
}

/**
 * 切换登录/注册模式
 */
const toggleMode = () => {
  isRegister.value = !isRegister.value
  formRef.value.resetFields()
}

/**
 * 跳转到忘记密码页面
 */
const handleForgotPassword = () => {
  router.push('/reset-password')
}

/**
 * 返回登录模式
 */
const handleBackToLogin = () => {
  isRegister.value = false
  loginType.value = 'account'
  formRef.value.resetFields()
}

/**
 * 监听登录方式切换
 * 切换时清空相应的表单字段和验证
 */
watch(loginType, (type) => {
  if (!formRef.value) return
  if (type === 'email') {
    // 切换到邮箱登录，清空账号密码字段
    form.username = ''
    form.password = ''
    formRef.value.clearValidate(['username', 'password'])
  } else {
    // 切换到账号登录，清空邮箱验证码字段
    form.email = ''
    form.code = ''
    formRef.value.clearValidate(['email', 'code'])
    timer.value = 0
    sendingCode.value = false
    // 清除倒计时定时器
    if (codeInterval) {
      clearInterval(codeInterval)
      codeInterval = null
    }
  }
})

/**
 * 监听登录/注册模式切换
 * 切换时重置 Turnstile 验证
 */
watch(isRegister, () => {
  resetTurnstile()
  nextTick(() => {
    renderTurnstile()
  })
})
</script>

<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <!-- 主要容器 -->
    <div class="login-box">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <img :src="logo" class="brand-logo" />
            <div class="logo-overlay"></div>
          </div>
          <h1>{{ isRegister ? '欢迎加入' : '欢迎回来' }}</h1>
          <p class="brand-subtitle">{{ isRegister ? '开启您的航海之旅' : '船舶管理系统' }}</p>

          <!-- 装饰性波浪线条 -->
          <div class="wave-decoration">
            <div class="wave-line"></div>
            <div class="wave-line"></div>
            <div class="wave-line"></div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <!-- 表单标题 -->
        <div class="form-header">
          <h2>{{ isRegister ? '创建账户' : '用户登录' }}</h2>
          <p class="form-desc">{{ isRegister ? '填写以下信息完成注册' : '请输入您的登录信息' }}</p>
        </div>

        <!-- 表单内容 -->
        <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form">
          <!-- 登录方式切换（仅登录模式显示） -->
          <el-radio-group v-if="!isRegister" v-model="loginType" class="type-switch">
            <el-radio-button value="account">
              <el-icon><User /></el-icon>
              账号登录
            </el-radio-button>
            <el-radio-button value="email">
              <el-icon><Message /></el-icon>
              邮箱登录
            </el-radio-button>
          </el-radio-group>

          <!-- 用户名输入（注册或账号登录模式） -->
          <el-form-item v-if="isRegister || loginType==='account'" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>

          <!-- 密码输入（注册或账号登录模式） -->
          <el-form-item v-if="isRegister || loginType==='account'" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <!-- 确认密码（仅注册模式） -->
          <el-form-item v-if="isRegister" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <!-- 邮箱输入（注册或邮箱登录模式） -->
          <el-form-item v-if="isRegister || loginType==='email'" prop="email">
            <el-input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱地址"
              :prefix-icon="Message"
              class="custom-input"
            />
          </el-form-item>

          <!-- 邮箱验证码（注册或邮箱登录模式） -->
          <el-form-item v-if="isRegister || loginType==='email'" prop="code">
            <el-input
              v-model="form.code"
              placeholder="请输入邮箱验证码"
              :prefix-icon="Key"
              class="custom-input"
            >
              <template #append>
                <el-button
                  :disabled="timer>0 || !form.email"
                  @click="sendEmailCode"
                  class="code-btn"
                  :class="{ 'sending': timer>0 }"
                >
                  <el-icon v-if="timer>0"><Key class="is-loading" /></el-icon>
                  {{ timer>0 ? `重新发送(${timer}s)` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- Cloudflare Turnstile 人机验证（仅登录模式） -->
          <el-form-item v-if="!isRegister" class="turnstile-item">
            <div id="turnstile-container" class="turnstile-widget"></div>
          </el-form-item>

          <!-- 忘记密码链接（仅登录模式） -->
          <div v-if="!isRegister" class="forgot-password">
            <el-link underline="never" class="forgot-link" @click="handleForgotPassword">忘记密码？</el-link>
          </div>

          <!-- 提交按钮 -->
          <el-button
            class="submit-btn"
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
            :disabled="submitting"
          >
            <el-icon v-if="submitting"><Loading /></el-icon>
            {{ isRegister ? '立即注册' : '安全登录' }}
          </el-button>

          <!-- 底部链接 -->
          <div class="footer-links">
            <span class="footer-text">{{ isRegister ? '已有账号？' : '没有账号？' }}</span>
            <el-link @click="toggleMode" class="toggle-link">
              {{ isRegister ? '立即登录' : '立即注册' }}
            </el-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 底部版权信息 -->
    <div class="footer-copyright">
      © 2025 船舶管理系统. 保留所有权利.
    </div>
  </div>
</template>

<style scoped>
/* 全局容器样式 */
.login-container {
  height: 100vh;
  background: var(--bg-gradient);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  color: var(--text-primary);
}

/* 背景装饰形状 */
.bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 107, 107, 0.12);
  backdrop-filter: var(--glass-blur);
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation-delay: 0s;
  box-shadow: var(--shadow-lg);
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation-delay: 2s;
  box-shadow: var(--shadow-lg);
}

.shape-3 {
  width: 250px;
  height: 250px;
  top: 40%;
  right: -125px;
  animation-delay: 1s;
  box-shadow: var(--shadow-lg);
}

.shape-4 {
  width: 180px;
  height: 180px;
  bottom: 30%;
  left: -90px;
  animation-delay: 3s;
  box-shadow: var(--shadow-lg);
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要登录框 */
.login-box {
  width: 1000px;
  height: 650px;
  display: flex;
  border-radius: 24px;
  background: var(--card-bg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--card-border);
  position: relative;
  z-index: 10;
  overflow: hidden;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1.2;
  background: linear-gradient(180deg, rgba(255, 107, 107, 0.12) 0%, transparent 70%);
  padding: 40px;
  text-align: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--border-color);
}

.brand-content {
  position: relative;
  z-index: 1;
}

.logo-container {
  position: relative;
  margin-bottom: 30px;
}

.brand-logo {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid rgba(255, 107, 107, 0.35);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: var(--shadow-lg);
  animation: pulse 4s ease-in-out infinite;
}

.logo-overlay {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border-radius: 50%;
  border: 1px solid rgba(255, 107, 107, 0.25);
  animation: rotate 10s linear infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.brand-section h1 {
  color: var(--text-primary);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: none;
}

.brand-subtitle {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 40px;
}

/* 波浪装饰线条 */
.wave-decoration {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.wave-line {
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, var(--coral-600), transparent);
  border-radius: 2px;
  opacity: 0.4;
  animation: wave 2s ease-in-out infinite;
}

.wave-line:nth-child(2) { animation-delay: 0.3s; }
.wave-line:nth-child(3) { animation-delay: 0.6s; }

@keyframes wave {
  0%, 100% { transform: translateY(0px); opacity: 0.6; }
  50% { transform: translateY(-5px); opacity: 1; }
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  padding: 50px;
  background: transparent;
  position: relative;
}

/* 表单头部 */
.form-header {
  margin-bottom: 30px;
}

.form-header h2 {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.form-desc {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 登录方式切换 */
.type-switch {
  margin-bottom: 25px;
}

.type-switch .el-radio-button__inner {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.type-switch .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: rgba(255, 107, 107, 0.12);
  color: var(--text-primary);
  border-color: rgba(255, 107, 107, 0.45);
  box-shadow: var(--shadow-sm);
}

/* 自定义输入框 */
.custom-input .el-input__wrapper {
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.custom-input .el-input__wrapper:focus-within {
  border-color: var(--coral-400);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
  background: var(--bg-secondary);
}

/* 输入框内部文字颜色 */
.custom-input .el-input__inner {
  color: var(--text-primary);
  background: transparent;
}

.custom-input .el-input__inner::placeholder {
  color: var(--text-tertiary);
}

.custom-input .el-input__prefix {
  color: var(--coral-500);
}

/* 验证码按钮 */
.code-btn {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: var(--coral-500);
  color: white;
  border: none;
}

.code-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.sending {
  background: rgba(161, 161, 166, 0.6);
  color: #fff !important;
}

.sending span,
.sending .el-icon {
  color: #fff !important;
}

.sending .el-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Turnstile Widget 样式 */
.turnstile-item {
  margin-bottom: 20px;
  background-color: transparent;
}

.turnstile-widget {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 65px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  padding: 10px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.turnstile-widget:hover {
  border-color: rgba(255, 107, 107, 0.45);
  box-shadow: var(--shadow-sm);
}

/* 忘记密码链接 */
.forgot-password {
  text-align: right;
  margin-bottom: 20px;
}

.forgot-link {
  color: var(--coral-600);
  font-weight: 600;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: var(--coral-700);
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  background: var(--coral-500);
  border: none;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 底部链接 */
.footer-links {
  text-align: center;
  margin-top: 24px;
  margin-bottom: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.footer-text {
  color: var(--text-secondary);
  margin-right: 8px;
}

.toggle-link {
  color: var(--coral-600);
  font-weight: 700;
  font-size: 16px;
}

/* 底部版权 */
.footer-copyright {
  position: absolute;
  bottom: 15px;
  color: var(--text-tertiary);
  font-size: 12px;
  z-index: 10;
}

/* 响应式设计 */
@media (max-width: 900px) {
  .login-box {
    width: 95%;
    height: auto;
    flex-direction: column;
  }

  .brand-section {
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }

  .form-section {
    padding: 30px;
  }
}

@media (max-width: 600px) {
  .brand-section h1 {
    font-size: 24px;
  }

  .form-header h2 {
    font-size: 22px;
  }

  .brand-logo {
    width: 90px;
    height: 90px;
  }
}
</style>
