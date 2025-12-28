<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="title">LMS 后台登录</h1>
      <p class="subtitle">使用管理员或教师/学生账号登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button
          type="primary"
          class="submit"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      <div class="hint">
        默认管理员：admin / admin123
        <el-button type="primary" text @click="goRegister">去注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { http } from '../services/http'
import { setSession } from '../services/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    try {
      const data = await http.post('/api/auth/login', {
        username: form.username,
        password: form.password,
      })
      setSession(data)
      ElMessage.success('登录成功')
      router.replace('/dashboard')
    } catch (error) {
      ElMessage.error('登录失败，请检查账号或密码')
    } finally {
      loading.value = false
    }
  })
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 24px;
}

.login-card {
  width: 380px;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.title {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #0f172a;
}

.subtitle {
  margin: 0 0 24px 0;
  color: #64748b;
  font-size: 14px;
}

.submit {
  width: 100%;
}

.hint {
  margin-top: 16px;
  color: #94a3b8;
  font-size: 12px;
  text-align: center;
}
</style>
