<template>
  <div class="student-center eduflow-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <div class="title">学员中心</div>
          <div class="subtitle">查看课程、资源、作业、测验与成绩</div>
        </div>
        <el-select v-model="selectedCourseId" placeholder="选择课程" @change="handleCourseChange">
          <el-option
            v-for="course in enrolledCourses"
            :key="course.id"
            :label="course.courseTitle"
            :value="course.courseId"
          />
        </el-select>
      </div>
    </el-card>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的课程" name="courses">
        <el-row :gutter="16">
          <el-col
            v-for="course in enrolledCourses"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-card shadow="hover" class="course-card">
              <div class="course-title">{{ course.courseTitle }}</div>
              <div class="course-meta">注册时间：{{ formatTime(course.createdAt) }}</div>
              <el-button type="primary" text @click="goToResources(course.courseId)">
                查看资源
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="课程资源" name="resources">
        <el-card shadow="hover">
          <el-table :data="resources" v-loading="loading.resources">
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="small" @click="previewResource(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="作业" name="assignments">
        <el-card shadow="hover">
          <el-table :data="assignments" v-loading="loading.assignments">
            <el-table-column prop="title" label="作业" min-width="220" />
            <el-table-column prop="dueAt" label="截止时间" width="180" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="openSubmit(row)">提交</el-button>
                <el-button size="small" text @click="showSubmission(row)">查看提交</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <span>我的作业提交</span>
          </template>
          <el-table :data="myAssignmentSubmissions">
            <el-table-column prop="assignmentId" label="作业ID" width="120" />
            <el-table-column prop="score" label="分数" width="120" />
            <el-table-column prop="submittedAt" label="提交时间" width="180" />
            <el-table-column prop="gradedAt" label="批改时间" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="测验" name="quizzes">
        <el-card shadow="hover">
          <el-table :data="quizzes" v-loading="loading.quizzes">
            <el-table-column prop="title" label="测验" min-width="220" />
            <el-table-column prop="startAt" label="开始时间" width="180" />
            <el-table-column prop="endAt" label="结束时间" width="180" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="openQuiz(row)">答题</el-button>
                <el-button size="small" text @click="showQuizSubmission(row)">查看成绩</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <span>我的测验提交</span>
          </template>
          <el-table :data="myQuizSubmissions">
            <el-table-column prop="quizId" label="测验ID" width="120" />
            <el-table-column prop="score" label="分数" width="120" />
            <el-table-column prop="submittedAt" label="提交时间" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="成绩统计" name="grades">
        <el-card shadow="hover" v-loading="loading.grades">
          <div class="grade-section">
            <div class="section-title">作业成绩</div>
            <el-table :data="gradeOverview.assignments">
              <el-table-column prop="assignmentId" label="作业ID" width="120" />
              <el-table-column prop="score" label="分数" width="120" />
              <el-table-column prop="submittedAt" label="提交时间" width="180" />
              <el-table-column prop="gradedAt" label="批改时间" width="180" />
            </el-table>
          </div>
          <div class="grade-section">
            <div class="section-title">测验成绩</div>
            <el-table :data="gradeOverview.quizzes">
              <el-table-column prop="quizId" label="测验ID" width="120" />
              <el-table-column prop="score" label="分数" width="120" />
              <el-table-column prop="submittedAt" label="提交时间" width="180" />
            </el-table>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="submitDialog.visible" title="提交作业" width="520px">
      <el-form :model="submitDialog.form" label-width="90px">
        <el-form-item label="内容">
          <el-input v-model="submitDialog.form.content" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="附件链接">
          <el-input v-model="submitDialog.form.attachmentUrl" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignment">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="quizDialog.visible" title="测验答题" width="680px">
      <div v-if="quizDialog.loading" class="dialog-loading">加载中...</div>
      <div v-else>
        <div v-for="question in quizDialog.questions" :key="question.id" class="question">
          <div class="question-title">
            {{ question.content }}
            <span class="question-type">{{ question.type }}</span>
          </div>
          <div v-if="question.type === 'SINGLE'">
            <el-radio-group v-model="quizDialog.answers[question.id]">
              <el-radio
                v-for="option in question.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="question.type === 'MULTI'">
            <el-checkbox-group v-model="quizDialog.answers[question.id]">
              <el-checkbox
                v-for="option in question.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div v-else>
            <el-input v-model="quizDialog.answers[question.id]" type="textarea" rows="3" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="quizDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitQuiz">提交测验</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialog.visible" title="资源预览" width="600px">
      <div class="preview">
        <div class="preview-row">
          <span class="label">标题：</span>
          <span>{{ previewDialog.item?.title }}</span>
        </div>
        <div class="preview-row" v-if="previewDialog.item?.url">
          <span class="label">链接：</span>
          <a :href="previewDialog.item.url" target="_blank" rel="noreferrer">
            {{ previewDialog.item.url }}
          </a>
        </div>
        <div class="preview-row" v-if="previewDialog.item?.type === 'AUDIO' && previewDialog.item?.url">
          <audio controls :src="previewDialog.item.url"></audio>
        </div>
        <div class="preview-row" v-if="previewDialog.item?.type === 'VIDEO' && previewDialog.item?.url">
          <video controls :src="previewDialog.item.url" style="max-width: 100%"></video>
        </div>
        <div class="preview-row" v-if="previewDialog.item?.content">
          <span class="label">内容：</span>
          <div class="content">{{ previewDialog.item.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../services/http'

const router = useRouter()
const activeTab = ref('courses')
const enrolledCourses = ref([])
const selectedCourseId = ref(null)

const resources = ref([])
const assignments = ref([])
const quizzes = ref([])
const myAssignmentSubmissions = ref([])
const myQuizSubmissions = ref([])
const gradeOverview = reactive({ assignments: [], quizzes: [] })

const loading = reactive({
  resources: false,
  assignments: false,
  quizzes: false,
  grades: false,
})

const submitDialog = reactive({
  visible: false,
  assignmentId: null,
  form: {
    content: '',
    attachmentUrl: '',
  },
})

const quizDialog = reactive({
  visible: false,
  quizId: null,
  loading: false,
  questions: [],
  answers: {},
})

const previewDialog = reactive({
  visible: false,
  item: null,
})

const handleCourseChange = () => {
  if (!selectedCourseId.value) {
    return
  }
  fetchResources()
  fetchAssignments()
  fetchQuizzes()
  fetchGrades()
}

const fetchEnrollments = async () => {
  try {
    const data = await http.get('/api/enrollments/my')
    enrolledCourses.value = data || []
    if (!selectedCourseId.value && enrolledCourses.value.length) {
      selectedCourseId.value = enrolledCourses.value[0].courseId
      handleCourseChange()
    }
  } catch (error) {
    ElMessage.error('课程加载失败')
  }
}

const fetchResources = async () => {
  loading.resources = true
  try {
    const data = await http.get(`/api/courses/${selectedCourseId.value}/resources`)
    resources.value = (data || []).map((item) => ({
      ...item,
      createdAt: item.createdAt ? formatTime(item.createdAt) : '',
    }))
  } catch (error) {
    ElMessage.error('资源加载失败')
  } finally {
    loading.resources = false
  }
}

const fetchAssignments = async () => {
  loading.assignments = true
  try {
    const data = await http.get(`/api/assignments/courses/${selectedCourseId.value}`)
    const submissions = await http.get('/api/assignments/submissions/my')
    myAssignmentSubmissions.value = (submissions || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
      gradedAt: formatTime(item.gradedAt),
    }))
    const submissionMap = new Map(
      myAssignmentSubmissions.value.map((item) => [item.assignmentId, item]),
    )
    assignments.value = (data || []).map((item) => ({
      ...item,
      dueAt: formatTime(item.dueAt),
      status: submissionMap.has(item.id) ? '已提交' : '未提交',
    }))
  } catch (error) {
    ElMessage.error('作业加载失败')
  } finally {
    loading.assignments = false
  }
}

const fetchQuizzes = async () => {
  loading.quizzes = true
  try {
    const data = await http.get(`/api/quizzes/courses/${selectedCourseId.value}`)
    const submissions = await http.get('/api/quizzes/submissions/my')
    myQuizSubmissions.value = (submissions || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
    }))
    const submissionMap = new Map(myQuizSubmissions.value.map((item) => [item.quizId, item]))
    quizzes.value = (data || []).map((item) => ({
      ...item,
      startAt: formatTime(item.startAt),
      endAt: formatTime(item.endAt),
      status: submissionMap.has(item.id) ? '已提交' : '未提交',
    }))
  } catch (error) {
    ElMessage.error('测验加载失败')
  } finally {
    loading.quizzes = false
  }
}

const fetchGrades = async () => {
  loading.grades = true
  try {
    const data = await http.get(`/api/grades/my?courseId=${selectedCourseId.value}`)
    gradeOverview.assignments = (data.assignments || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
      gradedAt: formatTime(item.gradedAt),
    }))
    gradeOverview.quizzes = (data.quizzes || []).map((item) => ({
      ...item,
      submittedAt: formatTime(item.submittedAt),
    }))
  } catch (error) {
    ElMessage.error('成绩加载失败')
  } finally {
    loading.grades = false
  }
}

const openSubmit = (assignment) => {
  submitDialog.assignmentId = assignment.id
  submitDialog.form.content = ''
  submitDialog.form.attachmentUrl = ''
  submitDialog.visible = true
}

const submitAssignment = async () => {
  try {
    await http.post('/api/assignments/submit', {
      assignmentId: submitDialog.assignmentId,
      content: submitDialog.form.content,
      attachmentUrl: submitDialog.form.attachmentUrl,
    })
    ElMessage.success('提交成功')
    submitDialog.visible = false
    fetchAssignments()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const showSubmission = (assignment) => {
  const submission = myAssignmentSubmissions.value.find(
    (item) => item.assignmentId === assignment.id,
  )
  if (!submission) {
    ElMessage.info('暂无提交记录')
    return
  }
  ElMessage.info(`分数：${submission.score ?? '未批改'}`)
}

const openQuiz = async (quiz) => {
  if (quiz.status === '已提交') {
    ElMessage.info('该测验已提交')
    return
  }
  quizDialog.visible = true
  quizDialog.quizId = quiz.id
  quizDialog.loading = true
  quizDialog.questions = []
  quizDialog.answers = {}
  try {
    const data = await http.get(`/api/quizzes/${quiz.id}/questions`)
    quizDialog.questions = (data || []).map((question) => ({
      ...question,
      options: parseOptions(question.options),
    }))
  } catch (error) {
    ElMessage.error('题目加载失败')
  } finally {
    quizDialog.loading = false
  }
}

const submitQuiz = async () => {
  const answers = quizDialog.questions.map((question) => {
    const value = quizDialog.answers[question.id]
    let answer = value
    if (Array.isArray(value)) {
      answer = value.join(',')
    }
    return { questionId: question.id, answer }
  })
  try {
    await http.post('/api/quizzes/submit', {
      quizId: quizDialog.quizId,
      answers,
    })
    ElMessage.success('提交成功')
    quizDialog.visible = false
    fetchQuizzes()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const showQuizSubmission = (quiz) => {
  const submission = myQuizSubmissions.value.find((item) => item.quizId === quiz.id)
  if (!submission) {
    ElMessage.info('暂无提交记录')
    return
  }
  ElMessage.info(`分数：${submission.score ?? '未批改'}`)
}

const previewResource = (item) => {
  previewDialog.item = item
  previewDialog.visible = true
}

const goToResources = (courseId) => {
  router.push(`/courses/${courseId}/resources`)
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

const parseOptions = (raw) => {
  if (!raw) {
    return []
  }
  const separators = ['\n', ';', '|', ',']
  let parts = [raw]
  for (const separator of separators) {
    if (raw.includes(separator)) {
      parts = raw.split(separator)
      break
    }
  }
  return parts
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part, index) => {
      if (part.includes(':')) {
        const [key, ...rest] = part.split(':')
        return { value: key.trim(), label: `${key.trim()}. ${rest.join(':').trim()}` }
      }
      const value = String.fromCharCode(65 + index)
      return { value, label: `${value}. ${part}` }
    })
}

onMounted(fetchEnrollments)
</script>

<style scoped lang="scss">
.student-center {
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

.grade-section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #1e293b;
}

.question {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.question-title {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 8px;
}

.question-type {
  margin-left: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  color: #334155;
}

.label {
  width: 60px;
  color: #94a3b8;
}

.content {
  white-space: pre-wrap;
  color: #0f172a;
}

.dialog-loading {
  padding: 24px;
  text-align: center;
  color: #94a3b8;
}
</style>
