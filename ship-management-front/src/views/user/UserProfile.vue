<script setup>
/**
 * @file 用户资料页面组件
 * @description 提供用户个人中心功能，包括查看和修改基本信息、更换头像、修改密码
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, User, Lock } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { useUserStore } from '@/stores/user'

const activeTab = ref('info')
const updating = ref(false)
const userStore = useUserStore()
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  avatarUrl: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const validateConfirmPwd = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 5, max: 16, message: '长度在 5 到 16 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 5, max: 16, message: '长度在 5 到 16 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const getUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    if (res.code === 0 && res.data) {
       userInfo.value = { ...userInfo.value, ...res.data }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleUpdateInfo = async () => {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true
      try {
        await request.put('/user/update', userInfo.value)
        ElMessage.success('资料修改成功')
        if (userStore.user) {
            userStore.user.nickname = userInfo.value.nickname
        }
        getUserInfo()
      } catch (e) {
      } finally {
        updating.value = false
      }
    }
  })
}

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const uploadAvatar = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const uploadRes = await request.post('/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const newAvatarUrl = uploadRes.data
    
    await request.patch('/user/avatar', null, {
      params: { avatarUrl: newAvatarUrl }
    })

    userInfo.value.avatarUrl = newAvatarUrl
    if (userStore.user) {
        userStore.user.avatarUrl = newAvatarUrl
    }
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const handleUpdatePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true
      try {
        await request.patch('/user/password', null, {
          params: {
            oldPassword: pwdForm.oldPassword,
            newPassword: pwdForm.newPassword
          }
        })
        ElMessage.success('密码修改成功')
        resetPwdForm()
      } catch (e) {
      } finally {
        updating.value = false
      }
    }
  })
}

const resetPwdForm = () => {
  if (pwdFormRef.value) pwdFormRef.value.resetFields()
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <div class="user-profile-container">
    <div class="page-header">
       <div class="header-left">
          <h2 class="page-title">个人中心</h2>
          <p class="page-subtitle">管理您的个人资料和账户安全</p>
       </div>
    </div>

    <el-card shadow="never" class="profile-card">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="基本资料" name="info">
          <template #label>
              <div class="tab-label">
                  <el-icon><User /></el-icon>
                  <span>基本资料</span>
              </div>
          </template>
          <div class="info-container">
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                action="#"
                :http-request="uploadAvatar"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                <div class="avatar-overlay">
                    <el-icon><Plus /></el-icon>
                    <span>更换头像</span>
                </div>
              </el-upload>
              <div class="upload-tip">支持 JPG/PNG，不超过 2MB</div>
            </div>

            <div class="form-section">
              <el-form ref="infoFormRef" :model="userInfo" :rules="infoRules" label-width="100px" size="large" label-position="top">
                <el-form-item label="登录账号">
                  <el-input v-model="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="用户昵称" prop="nickname">
                  <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="联系邮箱" prop="email">
                  <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item style="margin-top: 32px">
                  <el-button type="primary" :loading="updating" @click="handleUpdateInfo" style="width: 120px">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
            <template #label>
                <div class="tab-label">
                    <el-icon><Lock /></el-icon>
                    <span>修改密码</span>
                </div>
            </template>
          <div class="password-container">
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="120px" size="large" label-position="top">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="5-16位非空字符" />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item style="margin-top: 32px">
                <el-button type="primary" :loading="updating" @click="handleUpdatePwd" style="width: 120px">确认修改</el-button>
                <el-button @click="resetPwdForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.user-profile-container { padding: 0; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.header-left .page-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.header-left .page-subtitle { font-size: 14px; color: var(--text-secondary); margin: 0; }

.profile-card { min-height: 600px; border: none !important; box-shadow: var(--card-shadow) !important; border-radius: var(--border-radius-lg) !important; }
.profile-tabs { padding: 8px; }

.tab-label { display: flex; align-items: center; gap: 6px; }

.info-container { display: flex; margin-top: 32px; gap: 60px; }

.avatar-section {
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader { position: relative; display: block; width: 140px; height: 140px; border-radius: 50%; overflow: hidden; cursor: pointer; border: 4px solid var(--bg-color); box-shadow: 0 0 0 1px var(--border-color); transition: all 0.3s; }
.avatar-uploader:hover .avatar-overlay { opacity: 1; }
.avatar-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); color: white; display: flex; flex-direction: column; justify-content: center; align-items: center; opacity: 0; transition: opacity 0.3s; gap: 4px; }

.avatar { width: 100%; height: 100%; object-fit: cover; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 140px; height: 140px; text-align: center; line-height: 140px; background: var(--bg-color); }

.upload-tip { margin-top: 16px; font-size: 13px; color: var(--text-secondary); text-align: center; }

.form-section { flex: 1; max-width: 480px; }
.password-container { margin-top: 32px; max-width: 480px; margin-left: 20px; }
</style>
