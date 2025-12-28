<template>
  <div class="register-page">
    <div class="register-card">
      <h1 class="title">注册账号</h1>
      <p class="subtitle">创建学生或教师账号</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="studentNumber">
          <el-input v-model="form.studentNumber" placeholder="学号 / 工号（选填）" />
        </el-form-item>
        <el-form-item prop="className">
          <el-input v-model="form.className" placeholder="班级（选填，学生建议填写）" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="选择角色">
            <el-option label="学生" value="ROLE_STUDENT" />
            <el-option label="教师" value="ROLE_TEACHER" />
          </el-select>
        </el-form-item>
        <el-button
          type="primary"
          class="submit"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>
      <div class="hint">
        已有账号？
        <el-button type="primary" text @click="goLogin">去登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, User } from '@element-plus/icons-vue'
import { http } from '../services/http'
import { setSession } from '../services/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  studentNumber: '',
  className: '',
  password: '',
  role: 'ROLE_STUDENT',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const handleRegister = async () => {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    try {
      const data = await http.post('/api/auth/register', {
        username: form.username,
        email: form.email,
        studentNumber: form.studentNumber,
        className: form.className,
        password: form.password,
        role: form.role,
      })
      setSession(data)
      ElMessage.success('注册成功')
      router.replace('/dashboard')
    } catch (error) {
      ElMessage.error('注册失败，请检查填写信息')
    } finally {
      loading.value = false
    }
  })
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 24px;
}

.register-card {
  width: 420px;
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
