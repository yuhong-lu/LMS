<template>
  <div class="admin-center eduflow-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <div class="title">管理员中心</div>
          <div class="subtitle">学生与教师管理</div>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="学生管理" name="students">
        <el-row :gutter="16">
          <el-col :xs="24" :md="10">
            <el-card shadow="hover">
              <template #header>
                <div class="table-header">
                  <span>学生列表</span>
                  <el-input v-model="studentSearch" size="small" placeholder="搜索学生" clearable />
                </div>
              </template>
              <el-table :data="filteredStudents" v-loading="loading.users" height="520">
                <el-table-column prop="username" label="用户名" min-width="120" />
                <el-table-column prop="email" label="邮箱" min-width="160" />
                <el-table-column prop="studentNumber" label="学号/工号" min-width="120" />
                <el-table-column prop="className" label="班级" min-width="120" />
                <el-table-column label="选择" width="120">
                  <template #default="{ row }">
                    <el-button size="small" @click="selectStudent(row)">选择</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="14">
            <el-card shadow="hover">
              <template #header>
                <div class="table-header">
                  <span>课程与注册</span>
                  <el-select v-model="selectedCourseId" placeholder="选择课程" @change="fetchEnrollments">
                    <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
                  </el-select>
                </div>
              </template>

              <div class="assign-box">
                <div class="assign-label">当前学生：</div>
                <div class="assign-value">{{ selectedStudent?.username || '未选择' }}</div>
                <el-button type="primary" :disabled="!canAssign" @click="assignCourse">
                  分配注册
                </el-button>
              </div>

              <el-table :data="enrollments" v-loading="loading.enrollments">
                <el-table-column prop="studentUsername" label="学生" min-width="140" />
                <el-table-column prop="createdAt" label="注册时间" width="180" />
                <el-table-column label="操作" width="140">
                  <template #default="{ row }">
                    <el-button size="small" type="danger" plain @click="removeEnrollment(row)">
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="教师管理" name="teachers">
        <el-card shadow="hover">
          <template #header>
            <div class="table-header">
              <span>教师账号</span>
              <el-input v-model="teacherSearch" size="small" placeholder="搜索教师" clearable />
            </div>
          </template>
          <el-table :data="filteredTeachers" v-loading="loading.teachers">
            <el-table-column prop="username" label="用户名" min-width="140" />
            <el-table-column prop="email" label="邮箱" min-width="200" />
            <el-table-column prop="role" label="当前角色" width="120" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="promoteTeacher(row)" :disabled="row.role === 'ROLE_ADMIN'">
                  升级为管理员
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../services/http'

const activeTab = ref('students')
const students = ref([])
const teachers = ref([])
const courses = ref([])
const enrollments = ref([])
const selectedStudent = ref(null)
const selectedCourseId = ref(null)
const studentSearch = ref('')
const teacherSearch = ref('')

const loading = reactive({
  users: false,
  teachers: false,
  enrollments: false,
})

const filteredStudents = computed(() => {
  const keyword = studentSearch.value.trim().toLowerCase()
  if (!keyword) {
    return students.value
  }
  return students.value.filter((item) =>
    [item.username, item.email, item.studentNumber, item.className]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(keyword))
  )
})

const filteredTeachers = computed(() => {
  const keyword = teacherSearch.value.trim().toLowerCase()
  if (!keyword) {
    return teachers.value
  }
  return teachers.value.filter((item) => item.username.toLowerCase().includes(keyword))
})

const canAssign = computed(() => selectedStudent.value && selectedCourseId.value)

const fetchUsers = async () => {
  loading.users = true
  try {
    const data = await http.get('/api/admin/users')
    students.value = (data || []).filter((item) => item.role === 'ROLE_STUDENT') || []
    teachers.value = (data || []).filter((item) => item.role === 'ROLE_TEACHER' || item.role === 'ROLE_ADMIN') || []
  } catch (error) {
    ElMessage.error('学生加载失败')
  } finally {
    loading.users = false
  }
}

const fetchCourses = async () => {
  try {
    const data = await http.get('/api/courses')
    courses.value = data || []
    if (!selectedCourseId.value && courses.value.length) {
      selectedCourseId.value = courses.value[0].id
      fetchEnrollments()
    }
  } catch (error) {
    ElMessage.error('课程加载失败')
  }
}

const fetchEnrollments = async () => {
  if (!selectedCourseId.value) {
    return
  }
  loading.enrollments = true
  try {
    const data = await http.get(`/api/enrollments/courses/${selectedCourseId.value}`)
    enrollments.value = (data || []).map((item) => ({
      ...item,
      createdAt: formatTime(item.createdAt),
    }))
  } catch (error) {
    ElMessage.error('注册列表加载失败')
  } finally {
    loading.enrollments = false
  }
}

const selectStudent = (student) => {
  selectedStudent.value = student
}

const assignCourse = async () => {
  try {
    await http.post('/api/enrollments', {
      courseId: selectedCourseId.value,
      studentId: selectedStudent.value.id,
    })
    ElMessage.success('分配成功')
    fetchEnrollments()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const removeEnrollment = async (enrollment) => {
  try {
    await ElMessageBox.confirm('确认移除该注册记录吗？', '提示', { type: 'warning' })
    await http.del(`/api/enrollments/${enrollment.id}`)
    ElMessage.success('移除成功')
    fetchEnrollments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const promoteTeacher = async (teacher) => {
  try {
    await http.post(`/api/admin/users/${teacher.id}/promote-admin`)
    ElMessage.success('已升级为管理员')
    fetchUsers()
  } catch (error) {
    ElMessage.error('升级失败')
  }
}

const formatTime = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

onMounted(() => {
  fetchUsers()
  fetchCourses()
})
</script>

<style scoped lang="scss">
.admin-center {
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
  font-weight: 600;
  color: #0f172a;
}

.subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.assign-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fafc;
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 12px;
}

.assign-label {
  color: #94a3b8;
  font-size: 12px;
}

.assign-value {
  font-weight: 600;
  color: #1e293b;
}
</style>
