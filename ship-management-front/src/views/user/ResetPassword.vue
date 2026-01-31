<script setup>
/**
 * @file 重置密码页面组件
 * @description 提供用户通过邮箱验证码重置密码的功能，无需登录即可访问
 * @author Claude Code
 * @date 2024
 */

import { ref, reactive } from 'vue'
import { Message, Key, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { sendCodeService, resetPasswordByEmailService } from '@/api/user'

// 获取路由实例
const router = useRouter()

// 表单数据
const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证码倒计时
const timer = ref(0)

// 发送验证码加载状态
const sendingCode = ref(false)

// 提交表单加载状态
const submitting = ref(false)

// 表单验证规则
const rules = {
  email: [
    { required: true, type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 表单引用
const formRef = ref()

/**
 * 发送邮箱验证码
 * 先验证邮箱格式，再调用后端接口发送验证码
 */
const sendEmailCode = async () => {
  await formRef.value.validateField('email')
  sendingCode.value = true
  try {
    await sendCodeService(form.email)
    ElMessage.success('验证码已发送至您的邮箱')
    timer.value = 60
    // 开始倒计时
    const interval = setInterval(() => {
      timer.value--
      if (timer.value <= 0) {
        clearInterval(interval)
        sendingCode.value = false
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请稍后重试')
    sendingCode.value = false
  }
}

/**
 * 提交重置密码请求
 */
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true

  // 添加调试信息
  console.log('重置密码请求参数:', {
    email: form.email,
    code: form.code,
    newPassword: form.newPassword
  })

  try {
    const response = await resetPasswordByEmailService({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    })

    console.log('重置密码响应:', response)

    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } catch (error) {
    console.error('重置密码失败:', error)
    console.error('错误详情:', error.response?.data || error.message)

    // 根据错误类型显示不同的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.response?.status === 400) {
      ElMessage.error('请求参数错误，请检查邮箱和验证码')
    } else if (error.response?.status === 404) {
      ElMessage.error('用户不存在或验证码已过期')
    } else {
      ElMessage.error('密码重置失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 返回登录页面
 */
const handleBackToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="reset-password-container">
    <!-- 背景装饰 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 主要容器 -->
    <div class="reset-box">
      <!-- 左侧品牌展示 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1>重置密码</h1>
          <p class="brand-subtitle">通过邮箱验证找回您的账户</p>

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
        <!-- 表单头部 -->
        <div class="form-header">
          <h2>安全验证</h2>
          <p class="form-desc">请输入您的邮箱地址，我们将发送验证码</p>
        </div>

        <!-- 表单内容 -->
        <el-form ref="formRef" :model="form" :rules="rules" size="large" class="reset-form">
          <!-- 邮箱输入 -->
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              type="email"
              placeholder="请输入您的邮箱地址"
              :prefix-icon="Message"
              class="custom-input"
            />
          </el-form-item>

          <!-- 邮箱验证码 -->
          <el-form-item prop="code">
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

          <!-- 新密码 -->
          <el-form-item prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <!-- 确认新密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-button
            class="submit-btn"
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
            :disabled="submitting"
          >
            <el-icon v-if="submitting"><Loading /></el-icon>
            重置密码
          </el-button>

          <!-- 返回登录链接 -->
          <div class="back-to-login">
            <el-link @click="handleBackToLogin" class="back-link">
              <el-icon><ArrowLeft /></el-icon>
              返回登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 底部版权信息 -->
    <div class="footer-copyright">
      © 2024 船舶管理系统. 保留所有权利.
    </div>
  </div>
</template>

<style scoped>
/* 全局容器样式 */
.reset-password-container {
  height: 100vh;
  background: linear-gradient(135deg, #fdfcfb 0%, #e2d5c5 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
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
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation-delay: 0s;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation-delay: 2s;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.shape-3 {
  width: 250px;
  height: 250px;
  top: 40%;
  right: -125px;
  animation-delay: 1s;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要重置框 */
.reset-box {
  width: 1000px;
  height: 600px;
  display: flex;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 10;
  overflow: hidden;
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1.2;
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  padding: 40px;
  text-align: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-section h1 {
  color: #333;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: none;
}

.brand-subtitle {
  color: #666;
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
  background: linear-gradient(90deg, #333, transparent);
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
  background: #fff;
  position: relative;
}

/* 表单头部 */
.form-header {
  margin-bottom: 30px;
}

.form-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.form-desc {
  color: #666;
  font-size: 14px;
}

/* 自定义输入框 */
.custom-input .el-input__wrapper {
  border-radius: 12px;
  padding: 12px 16px;
  border: 2px solid transparent;
  background: #f8f9fa;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.custom-input .el-input__wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: #fff;
}

.custom-input .el-input__prefix {
  color: #667eea;
}

/* 验证码按钮 */
.code-btn {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: #667eea;
  color: white;
  border: none;
}

.code-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.sending {
  background: linear-gradient(135deg, #a0a0a0, #808080);
  color: #fff !important;
}

.sending .el-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  background: #667eea;
  border: none;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 返回登录链接 */
.back-to-login {
  text-align: center;
  margin-top: 24px;
  margin-bottom: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.back-link {
  color: #667eea;
  font-weight: 700;
  font-size: 15px;
  transition: color 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.back-link:hover {
  color: #5568d3;
}

/* 底部版权 */
.footer-copyright {
  position: absolute;
  bottom: 20px;
  color: rgba(0, 0, 0, 0.6);
  font-size: 12px;
  z-index: 10;
}

/* 响应式设计 */
@media (max-width: 900px) {
  .reset-box {
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
}
</style>
