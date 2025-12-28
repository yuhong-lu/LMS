<template>
  <div class="teacher-center eduflow-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <div class="title">教师中心</div>
          <div class="subtitle">课程管理、作业、测验与成绩统计</div>
        </div>
        <div class="header-actions">
          <div class="course-selector">
            <div class="course-label">当前课程</div>
            <el-select
              v-model="selectedCourseId"
              class="course-select"
              placeholder="请选择课程"
              @change="handleCourseChange"
            >
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.title"
                :value="course.id"
              />
            </el-select>
          </div>
          <el-button v-if="isAdmin" type="primary" @click="openCourseDialog">新建课程</el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的课程" name="courses">
        <el-row :gutter="16">
          <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8">
            <el-card shadow="hover" class="course-card">
              <div class="course-title">{{ course.title }}</div>
              <div class="course-meta">更新时间：{{ formatTime(course.updatedAt) }}</div>
              <div class="course-actions">
                <el-button size="small" @click="openEditCourse(course)">编辑</el-button>
                <el-button size="small" type="primary" plain @click="goToResources(course.id)">
                  资源管理
                </el-button>
                <el-button size="small" type="danger" plain @click="removeCourse(course)">
                  删除
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="注册管理" name="enrollments">
        <el-row :gutter="16">
          <el-col :xs="24" :md="10">
            <el-card shadow="hover">
              <template #header>
                <div class="table-header">
                  <span>学生列表</span>
                  <el-input v-model="studentSearch" size="small" placeholder="搜索学生" clearable />
                </div>
              </template>
              <el-table :data="filteredStudents" v-loading="loading.students" height="520">
                <el-table-column prop="username" label="用户名" min-width="120" />
                <el-table-column prop="email" label="邮箱" min-width="160" />
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
                  <div class="table-title">
                    <span>课程注册</span>
                    <span class="table-course" v-if="currentCourseName">
                      当前课程：{{ currentCourseName }}
                    </span>
                  </div>
                  <el-button type="primary" :disabled="!canAssign" @click="assignCourse">
                    分配注册
                  </el-button>
                </div>
              </template>
              <div class="assign-box">
                <div class="assign-label">当前学生：</div>
                <div class="assign-value">{{ selectedStudent?.username || '未选择' }}</div>
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

      <el-tab-pane label="作业" name="assignments">
        <el-card shadow="hover">
          <div class="table-header">
            <span>课程作业</span>
            <div class="table-actions">
              <el-button type="primary" @click="openAssignmentDialog">发布作业</el-button>
              <el-button @click="exportSubmissions">导出提交</el-button>
            </div>
          </div>
          <el-table :data="assignments" v-loading="loading.assignments">
            <el-table-column prop="title" label="作业" min-width="220" />
            <el-table-column prop="dueAt" label="截止时间" width="180" />
            <el-table-column label="操作" width="260">
              <template #default="{ row }">
                <el-button size="small" @click="openEditAssignment(row)">编辑</el-button>
                <el-button size="small" text @click="openSubmissions(row)">提交列表</el-button>
                <el-button size="small" type="danger" plain @click="removeAssignment(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="测验" name="quizzes">
        <el-card shadow="hover">
          <div class="table-header">
            <span>课程测验</span>
            <el-button type="primary" @click="openQuizDialog">发布测验</el-button>
          </div>
          <el-table :data="quizzes" v-loading="loading.quizzes">
            <el-table-column prop="title" label="测验" min-width="220" />
            <el-table-column prop="startAt" label="开始时间" width="180" />
            <el-table-column prop="endAt" label="结束时间" width="180" />
            <el-table-column label="操作" width="320">
              <template #default="{ row }">
                <el-button size="small" @click="openEditQuiz(row)">编辑</el-button>
                <el-button size="small" text @click="openQuestions(row)">题库</el-button>
                <el-button size="small" text @click="openQuizSubmissions(row)">提交</el-button>
                <el-button size="small" type="danger" plain @click="removeQuiz(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="成绩统计" name="grades">
        <el-card shadow="hover" v-loading="loading.grades">
          <div class="summary-grid">
            <div class="summary-item">
              <div class="summary-label">作业提交数</div>
              <div class="summary-value">{{ gradeSummary.assignmentSubmissions }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">测验提交数</div>
              <div class="summary-value">{{ gradeSummary.quizSubmissions }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">作业平均分</div>
              <div class="summary-value">{{ gradeSummary.assignmentAverage ?? '-' }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">测验平均分</div>
              <div class="summary-value">{{ gradeSummary.quizAverage ?? '-' }}</div>
            </div>
          </div>
          <el-table :data="studentSummaries">
            <el-table-column prop="studentUsername" label="学生" min-width="160" />
            <el-table-column prop="assignmentAverage" label="作业均分" width="140" />
            <el-table-column prop="quizAverage" label="测验均分" width="140" />
            <el-table-column prop="assignmentSubmissions" label="作业提交" width="120" />
            <el-table-column prop="quizSubmissions" label="测验提交" width="120" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="courseDialog.visible" :title="courseDialog.title" width="520px">
      <el-form :model="courseDialog.form" label-width="90px">
        <el-form-item v-if="isAdmin && !courseDialog.courseId" label="授课教师">
          <el-select v-model="courseDialog.form.teacherId" placeholder="选择教师">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="`${teacher.username} (${teacher.email})`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="courseDialog.form.title" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="courseDialog.form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="大纲">
          <el-input v-model="courseDialog.form.syllabus" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCourse">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignmentDialog.visible" :title="assignmentDialog.title" width="520px">
      <el-form :model="assignmentDialog.form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="assignmentDialog.form.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="assignmentDialog.form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="assignmentDialog.form.dueAt" type="datetime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignmentDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignment">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submissionsDialog.visible" title="作业提交" width="720px">
      <el-table :data="submissionsDialog.items">
        <el-table-column prop="studentUsername" label="学生" min-width="160" />
        <el-table-column prop="score" label="分数" width="120" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openGrade(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="gradeDialog.visible" title="批改作业" width="420px">
      <el-form :model="gradeDialog.form" label-width="80px">
        <el-form-item label="分数">
          <el-input-number v-model="gradeDialog.form.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="反馈">
          <el-input v-model="gradeDialog.form.feedback" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="quizDialog.visible" :title="quizDialog.title" width="520px">
      <el-form :model="quizDialog.form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="quizDialog.form.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="quizDialog.form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="quizDialog.form.startAt" type="datetime" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="quizDialog.form.endAt" type="datetime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quizDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitQuiz">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="questionDialog.visible" title="题库管理" width="760px">
      <div class="table-header">
        <span>题目列表</span>
        <el-button type="primary" @click="openQuestionForm">新增题目</el-button>
      </div>
      <el-table :data="questionDialog.questions">
        <el-table-column prop="content" label="题目" min-width="260" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEditQuestion(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeQuestion(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="questionForm.visible" :title="questionForm.title" width="520px">
      <el-form :model="questionForm.form" label-width="90px">
        <el-form-item label="类型">
          <el-select v-model="questionForm.form.type">
            <el-option label="单选" value="SINGLE" />
            <el-option label="多选" value="MULTI" />
            <el-option label="简答" value="TEXT" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目">
          <el-input v-model="questionForm.form.content" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="选项">
          <el-input v-model="questionForm.form.options" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="questionForm.form.correctAnswer" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionForm.visible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="quizSubmissions.visible" title="测验提交" width="760px">
      <el-table :data="quizSubmissions.items">
        <el-table-column prop="studentUsername" label="学生" min-width="160" />
        <el-table-column prop="score" label="分数" width="120" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openAnswerGrade(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="answerDialog.visible" title="批改测验" width="720px">
      <div v-for="answer in answerDialog.answers" :key="answer.id" class="answer-item">
        <div class="answer-title">题目ID：{{ answer.questionId }}</div>
        <div class="answer-content">回答：{{ answer.answer || '-' }}</div>
        <el-input-number v-model="answer.score" :min="0" :max="5" size="small" />
        <el-checkbox v-model="answer.correct">正确</el-checkbox>
        <el-button size="small" @click="saveAnswer(answer)">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../services/http'
import { getUser } from '../services/auth'

const router = useRouter()
const user = getUser()
const isAdmin = computed(() => user?.roles?.includes('ROLE_ADMIN'))
const activeTab = ref('courses')
const courses = ref([])
const selectedCourseId = ref(null)
const teachers = ref([])

const students = ref([])
const enrollments = ref([])
const selectedStudent = ref(null)
const studentSearch = ref('')

const assignments = ref([])
const quizzes = ref([])
const studentSummaries = ref([])
const gradeSummary = reactive({
  assignmentSubmissions: 0,
  quizSubmissions: 0,
  assignmentAverage: null,
  quizAverage: null,
})

const loading = reactive({
  students: false,
  teachers: false,
  enrollments: false,
  assignments: false,
  quizzes: false,
  grades: false,
})

const courseDialog = reactive({
  visible: false,
  title: '新建课程',
  courseId: null,
  form: {
    title: '',
    description: '',
    syllabus: '',
    teacherId: null,
  },
})

const assignmentDialog = reactive({
  visible: false,
  title: '发布作业',
  assignmentId: null,
  form: {
    title: '',
    description: '',
    dueAt: '',
  },
})

const submissionsDialog = reactive({
  visible: false,
  assignmentId: null,
  items: [],
})

const gradeDialog = reactive({
  visible: false,
  submissionId: null,
  form: {
    score: 0,
    feedback: '',
  },
})

const quizDialog = reactive({
  visible: false,
  title: '发布测验',
  quizId: null,
  form: {
    title: '',
    description: '',
    startAt: '',
    endAt: '',
  },
})

const questionDialog = reactive({
  visible: false,
  quizId: null,
  questions: [],
})

const questionForm = reactive({
  visible: false,
  title: '新增题目',
  questionId: null,
  quizId: null,
  form: {
    type: 'SINGLE',
    content: '',
    options: '',
    correctAnswer: '',
  },
})

const quizSubmissions = reactive({
  visible: false,
  quizId: null,
  items: [],
})

const answerDialog = reactive({
  visible: false,
  answers: [],
})

const filteredStudents = computed(() => {
  const keyword = studentSearch.value.trim().toLowerCase()
  if (!keyword) {
    return students.value
  }
  return students.value.filter((item) => item.username.toLowerCase().includes(keyword))
})

const canAssign = computed(() => selectedStudent.value && selectedCourseId.value)
const currentCourseName = computed(() => {
  const match = courses.value.find((course) => course.id === selectedCourseId.value)
  return match ? match.title : ''
})

const fetchCourses = async () => {
  try {
    const data = await http.get('/api/courses')
    courses.value = data || []
    if (!selectedCourseId.value && courses.value.length) {
      selectedCourseId.value = courses.value[0].id
      handleCourseChange()
    }
  } catch (error) {
    ElMessage.error('课程加载失败')
  }
}

const fetchStudents = async () => {
  loading.students = true
  try {
    const data = await http.get('/api/users/students')
    students.value = data || []
  } catch (error) {
    ElMessage.error('学生加载失败')
  } finally {
    loading.students = false
  }
}

const fetchTeachers = async () => {
  if (!isAdmin.value) {
    return
  }
  loading.teachers = true
  try {
    const data = await http.get('/api/users/teachers')
    teachers.value = data || []
  } catch (error) {
    ElMessage.error('教师加载失败')
  } finally {
    loading.teachers = false
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

const handleCourseChange = () => {
  if (!selectedCourseId.value) {
    return
  }
  fetchEnrollments()
  fetchAssignments()
  fetchQuizzes()
  fetchGrades()
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

const openCourseDialog = () => {
  courseDialog.visible = true
  courseDialog.title = '新建课程'
  courseDialog.courseId = null
  courseDialog.form = { title: '', description: '', syllabus: '', teacherId: null }
}

const openEditCourse = (course) => {
  courseDialog.visible = true
  courseDialog.title = '编辑课程'
  courseDialog.courseId = course.id
  courseDialog.form = {
    title: course.title,
    description: course.description,
    syllabus: course.syllabus,
  }
}

const submitCourse = async () => {
  try {
    if (courseDialog.courseId) {
      await http.put(`/api/courses/${courseDialog.courseId}`, courseDialog.form)
    } else {
      if (!isAdmin.value) {
        ElMessage.error('仅管理员可新建课程')
        return
      }
      if (!courseDialog.form.teacherId) {
        ElMessage.error('请选择授课教师')
        return
      }
      await http.post('/api/courses', courseDialog.form)
    }
    ElMessage.success('保存成功')
    courseDialog.visible = false
    fetchCourses()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeCourse = async (course) => {
  try {
    await ElMessageBox.confirm(`确认删除课程「${course.title}」吗？`, '提示', {
      type: 'warning',
    })
    await http.del(`/api/courses/${course.id}`)
    ElMessage.success('删除成功')
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const goToResources = (courseId) => {
  router.push(`/courses/${courseId}/resources`)
}

const fetchAssignments = async () => {
  loading.assignments = true
  try {
    const data = await http.get(`/api/assignments/courses/${selectedCourseId.value}`)
    assignments.value = (data || []).map((item) => ({
      ...item,
      dueAtRaw: item.dueAt,
      dueAt: formatTime(item.dueAt),
    }))
  } catch (error) {
    ElMessage.error('作业加载失败')
  } finally {
    loading.assignments = false
  }
}

const openAssignmentDialog = () => {
  assignmentDialog.visible = true
  assignmentDialog.title = '发布作业'
  assignmentDialog.assignmentId = null
  assignmentDialog.form = { title: '', description: '', dueAt: '' }
}

const openEditAssignment = (assignment) => {
  assignmentDialog.visible = true
  assignmentDialog.title = '编辑作业'
  assignmentDialog.assignmentId = assignment.id
  assignmentDialog.form = {
    title: assignment.title,
    description: assignment.description,
    dueAt: toDate(assignment.dueAtRaw),
  }
}

const submitAssignment = async () => {
  try {
    const payload = {
      ...assignmentDialog.form,
      dueAt: toIso(assignmentDialog.form.dueAt),
    }
    if (assignmentDialog.assignmentId) {
      await http.put(`/api/assignments/${assignmentDialog.assignmentId}`, payload)
    } else {
      await http.post(`/api/assignments/courses/${selectedCourseId.value}`, payload)
    }
    ElMessage.success('保存成功')
    assignmentDialog.visible = false
    fetchAssignments()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeAssignment = async (assignment) => {
  try {
    await ElMessageBox.confirm(`确认删除作业「${assignment.title}」吗？`, '提示', {
      type: 'warning',
    })
    await http.del(`/api/assignments/${assignment.id}`)
    ElMessage.success('删除成功')
    fetchAssignments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openSubmissions = async (assignment) => {
  submissionsDialog.visible = true
  submissionsDialog.assignmentId = assignment.id
  try {
    const data = await http.get(`/api/assignments/${assignment.id}/submissions`)
    submissionsDialog.items = (data || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
    }))
  } catch (error) {
    ElMessage.error('提交加载失败')
  }
}

const openGrade = (submission) => {
  gradeDialog.visible = true
  gradeDialog.submissionId = submission.id
  gradeDialog.form = {
    score: submission.score ?? 0,
    feedback: submission.feedback || '',
  }
}

const submitGrade = async () => {
  try {
    await http.put(`/api/assignments/submissions/${gradeDialog.submissionId}/grade`, gradeDialog.form)
    ElMessage.success('保存成功')
    gradeDialog.visible = false
    openSubmissions({ id: submissionsDialog.assignmentId })
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const exportSubmissions = async () => {
  if (!selectedCourseId.value) {
    return
  }
  try {
    const csv = await http.get(`/api/assignments/courses/${selectedCourseId.value}/submissions/export`)
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `course-${selectedCourseId.value}-submissions.csv`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const fetchQuizzes = async () => {
  loading.quizzes = true
  try {
    const data = await http.get(`/api/quizzes/courses/${selectedCourseId.value}`)
    quizzes.value = (data || []).map((item) => ({
      ...item,
      startAtRaw: item.startAt,
      endAtRaw: item.endAt,
      startAt: formatTime(item.startAt),
      endAt: formatTime(item.endAt),
    }))
  } catch (error) {
    ElMessage.error('测验加载失败')
  } finally {
    loading.quizzes = false
  }
}

const openQuizDialog = () => {
  quizDialog.visible = true
  quizDialog.title = '发布测验'
  quizDialog.quizId = null
  quizDialog.form = { title: '', description: '', startAt: '', endAt: '' }
}

const openEditQuiz = (quiz) => {
  quizDialog.visible = true
  quizDialog.title = '编辑测验'
  quizDialog.quizId = quiz.id
  quizDialog.form = {
    title: quiz.title,
    description: quiz.description,
    startAt: toDate(quiz.startAtRaw),
    endAt: toDate(quiz.endAtRaw),
  }
}

const submitQuiz = async () => {
  try {
    const payload = {
      ...quizDialog.form,
      startAt: toIso(quizDialog.form.startAt),
      endAt: toIso(quizDialog.form.endAt),
    }
    if (quizDialog.quizId) {
      await http.put(`/api/quizzes/${quizDialog.quizId}`, payload)
    } else {
      await http.post(`/api/quizzes/courses/${selectedCourseId.value}`, payload)
    }
    ElMessage.success('保存成功')
    quizDialog.visible = false
    fetchQuizzes()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeQuiz = async (quiz) => {
  try {
    await ElMessageBox.confirm(`确认删除测验「${quiz.title}」吗？`, '提示', { type: 'warning' })
    await http.del(`/api/quizzes/${quiz.id}`)
    ElMessage.success('删除成功')
    fetchQuizzes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openQuestions = async (quiz) => {
  questionDialog.visible = true
  questionDialog.quizId = quiz.id
  await loadQuestions()
}

const loadQuestions = async () => {
  try {
    const data = await http.get(`/api/quizzes/${questionDialog.quizId}/questions`)
    questionDialog.questions = data || []
  } catch (error) {
    ElMessage.error('题库加载失败')
  }
}

const openQuestionForm = () => {
  questionForm.visible = true
  questionForm.title = '新增题目'
  questionForm.questionId = null
  questionForm.quizId = questionDialog.quizId
  questionForm.form = { type: 'SINGLE', content: '', options: '', correctAnswer: '' }
}

const openEditQuestion = (question) => {
  questionForm.visible = true
  questionForm.title = '编辑题目'
  questionForm.questionId = question.id
  questionForm.quizId = questionDialog.quizId
  questionForm.form = {
    type: question.type,
    content: question.content,
    options: question.options,
    correctAnswer: question.correctAnswer,
  }
}

const submitQuestion = async () => {
  try {
    if (questionForm.questionId) {
      await http.put(`/api/quizzes/questions/${questionForm.questionId}`, questionForm.form)
    } else {
      await http.post(`/api/quizzes/${questionForm.quizId}/questions`, questionForm.form)
    }
    ElMessage.success('保存成功')
    questionForm.visible = false
    loadQuestions()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeQuestion = async (question) => {
  try {
    await ElMessageBox.confirm('确认删除该题目吗？', '提示', { type: 'warning' })
    await http.del(`/api/quizzes/questions/${question.id}`)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openQuizSubmissions = async (quiz) => {
  quizSubmissions.visible = true
  quizSubmissions.quizId = quiz.id
  try {
    const data = await http.get(`/api/quizzes/${quiz.id}/submissions`)
    quizSubmissions.items = (data || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
    }))
  } catch (error) {
    ElMessage.error('提交加载失败')
  }
}

const openAnswerGrade = (submission) => {
  answerDialog.visible = true
  answerDialog.answers = (submission.answers || []).map((item) => ({
    ...item,
    correct: item.correct ?? false,
    score: item.score ?? 0,
  }))
}

const saveAnswer = async (answer) => {
  try {
    await http.put(`/api/quizzes/answers/${answer.id}/grade`, {
      score: answer.score,
      correct: answer.correct,
    })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const fetchGrades = async () => {
  loading.grades = true
  try {
    const summary = await http.get(`/api/grades/courses/${selectedCourseId.value}/summary`)
    Object.assign(gradeSummary, summary)
    const data = await http.get(`/api/grades/courses/${selectedCourseId.value}/students`)
    studentSummaries.value = data || []
  } catch (error) {
    ElMessage.error('成绩加载失败')
  } finally {
    loading.grades = false
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

const toDate = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '' : date
}

const toIso = (value) => {
  if (!value) {
    return null
  }
  if (value instanceof Date) {
    return value.toISOString()
  }
  return value
}

onMounted(() => {
  fetchCourses()
  fetchStudents()
  fetchTeachers()
})
</script>

<style scoped lang="scss">
.teacher-center {
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
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.course-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.course-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
  min-width: 56px;
}

.course-select {
  min-width: 220px;
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

.course-card {
  margin-bottom: 16px;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.course-meta {
  margin: 8px 0 12px;
  font-size: 12px;
  color: #94a3b8;
}

.course-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.table-course {
  font-size: 12px;
  color: #0f172a;
  font-weight: 600;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-actions {
  display: flex;
  gap: 8px;
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
}

.summary-label {
  font-size: 12px;
  color: #94a3b8;
}

.summary-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.answer-item {
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.answer-title {
  font-weight: 600;
  color: #0f172a;
}

.answer-content {
  color: #475569;
}
</style>
