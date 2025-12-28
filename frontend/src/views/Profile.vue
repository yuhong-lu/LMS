<template>
  <div class="profile-page eduflow-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <div class="title">个人中心</div>
          <div class="subtitle">查看账户信息与角色</div>
        </div>
        <el-tag type="primary" effect="plain" round>{{ userInfo.role || '未知角色' }}</el-tag>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="soft-card">
          <template #header>
            <div class="table-header">
              <span>基础信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学号/工号">{{ userInfo.studentNumber || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ userInfo.className || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ userInfo.role || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(userInfo.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="hover" class="soft-card">
          <template #header>
            <div class="table-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="actions">
            <el-button type="primary" plain @click="refresh">刷新信息</el-button>
            <el-button type="danger" plain @click="logout">退出登录</el-button>
          </div>
          <div class="tip">如需修改邮箱、学号或班级，请联系管理员；当前版本不修改后端逻辑。</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../services/http'
import { clearSession } from '../services/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = reactive({
  username: '',
  email: '',
  studentNumber: '',
  className: '',
  role: '',
  createdAt: '',
})

const fetchProfile = async () => {
  try {
    const data = await http.get('/api/users/me')
    Object.assign(userInfo, data || {})
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const refresh = () => fetchProfile()

const logout = () => {
  clearSession()
  router.replace('/login')
}

const formatTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return val
  return d.toLocaleString()
}

onMounted(fetchProfile)
</script>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-card {
  background: #fff;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.subtitle {
  margin-top: 4px;
  color: #6b7280;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.actions {
  display: flex;
  gap: 12px;
}

.tip {
  margin-top: 12px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
