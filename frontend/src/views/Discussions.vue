<template>
  <div class="discussion-page eduflow-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <div class="title">课程讨论区</div>
          <div class="subtitle">选择课程后创建主题、参与讨论</div>
        </div>
        <div class="header-actions">
          <el-select v-model="selectedCourseId" placeholder="选择课程" @change="handleCourseChange">
            <el-option
              v-for="course in availableCourses"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
          <el-button type="primary" @click="openTopicDialog">发布主题</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card shadow="hover">
          <template #header>
            <div class="table-header">
              <span>讨论主题</span>
              <el-input
                v-model="searchQuery"
                placeholder="搜索主题"
                size="small"
                clearable
              />
            </div>
          </template>
          <el-table :data="filteredTopics" v-loading="loading.topics" height="540">
            <el-table-column prop="title" label="主题" min-width="200" />
            <el-table-column prop="authorUsername" label="作者" width="120" />
            <el-table-column prop="createdAt" label="时间" width="160" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="selectTopic(row)">查看</el-button>
                <el-button
                  size="small"
                  text
                  v-if="canManageTopic"
                  @click="openEditTopic(row)"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  text
                  type="danger"
                  v-if="canManageTopic"
                  @click="removeTopic(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="detail-card">
          <div v-if="currentTopic" class="topic-detail">
            <div class="topic-title">{{ currentTopic.title }}</div>
            <div class="topic-meta">
              <span>作者：{{ currentTopic.authorUsername }}</span>
              <span>发布时间：{{ currentTopic.createdAt }}</span>
            </div>
            <div class="topic-content">{{ currentTopic.content }}</div>
            <div class="reply-header">
              <span>讨论回复</span>
              <el-button size="small" type="primary" @click="openReplyDialog">回复</el-button>
            </div>
            <el-table :data="replies" v-loading="loading.replies">
              <el-table-column prop="authorUsername" label="用户" width="140" />
              <el-table-column prop="content" label="内容" min-width="220" />
              <el-table-column prop="createdAt" label="时间" width="160" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    text
                    type="danger"
                    v-if="canManageTopic"
                    @click="removeReply(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="empty-topic">请选择一个主题查看详情</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="topicDialog.visible" :title="topicDialog.title" width="520px">
      <el-form :model="topicDialog.form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="topicDialog.form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="topicDialog.form.content" type="textarea" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitTopic">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialog.visible" title="回复主题" width="520px">
      <el-form :model="replyDialog.form" label-width="80px">
        <el-form-item label="内容">
          <el-input v-model="replyDialog.form.content" type="textarea" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../services/http'
import { getUser } from '../services/auth'

const user = getUser()
const roles = user?.roles || []
const canManageTopic = roles.includes('ROLE_TEACHER') || roles.includes('ROLE_ADMIN')

const availableCourses = ref([])
const selectedCourseId = ref(null)
const topics = ref([])
const replies = ref([])
const currentTopic = ref(null)
const searchQuery = ref('')

const loading = reactive({
  topics: false,
  replies: false,
})

const topicDialog = reactive({
  visible: false,
  title: '发布主题',
  topicId: null,
  form: {
    title: '',
    content: '',
  },
})

const replyDialog = reactive({
  visible: false,
  form: {
    content: '',
  },
})

const fetchCourses = async () => {
  try {
    const data = await http.get('/api/courses')
    availableCourses.value = data || []
    if (!selectedCourseId.value && availableCourses.value.length) {
      selectedCourseId.value = availableCourses.value[0].id
      handleCourseChange()
    }
  } catch (error) {
    ElMessage.error('课程加载失败')
  }
}

const handleCourseChange = () => {
  if (!selectedCourseId.value) {
    return
  }
  fetchTopics()
}

const fetchTopics = async () => {
  loading.topics = true
  try {
    const data = await http.get(`/api/discussions/courses/${selectedCourseId.value}/topics`)
    topics.value = (data || []).map((item) => ({
      ...item,
      createdAt: formatTime(item.createdAt),
    }))
    currentTopic.value = topics.value[0] || null
    if (currentTopic.value) {
      fetchReplies(currentTopic.value.id)
    } else {
      replies.value = []
    }
  } catch (error) {
    ElMessage.error('主题加载失败')
  } finally {
    loading.topics = false
  }
}

const fetchReplies = async (topicId) => {
  loading.replies = true
  try {
    const data = await http.get(`/api/discussions/topics/${topicId}/replies`)
    replies.value = (data || []).map((item) => ({
      ...item,
      createdAt: formatTime(item.createdAt),
    }))
  } catch (error) {
    ElMessage.error('回复加载失败')
  } finally {
    loading.replies = false
  }
}

const selectTopic = (topic) => {
  currentTopic.value = topic
  fetchReplies(topic.id)
}

const openTopicDialog = () => {
  topicDialog.visible = true
  topicDialog.title = '发布主题'
  topicDialog.topicId = null
  topicDialog.form = { title: '', content: '' }
}

const openEditTopic = (topic) => {
  topicDialog.visible = true
  topicDialog.title = '编辑主题'
  topicDialog.topicId = topic.id
  topicDialog.form = { title: topic.title, content: topic.content }
}

const submitTopic = async () => {
  if (!selectedCourseId.value) {
    return
  }
  try {
    if (topicDialog.topicId) {
      await http.put(`/api/discussions/topics/${topicDialog.topicId}`, topicDialog.form)
    } else {
      await http.post(`/api/discussions/courses/${selectedCourseId.value}/topics`, topicDialog.form)
    }
    ElMessage.success('保存成功')
    topicDialog.visible = false
    fetchTopics()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeTopic = async (topic) => {
  try {
    await ElMessageBox.confirm(`确认删除主题「${topic.title}」吗？`, '提示', { type: 'warning' })
    await http.del(`/api/discussions/topics/${topic.id}`)
    ElMessage.success('删除成功')
    fetchTopics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openReplyDialog = () => {
  replyDialog.visible = true
  replyDialog.form = { content: '' }
}

const submitReply = async () => {
  if (!currentTopic.value) {
    return
  }
  try {
    await http.post(`/api/discussions/topics/${currentTopic.value.id}/replies`, replyDialog.form)
    ElMessage.success('回复成功')
    replyDialog.visible = false
    fetchReplies(currentTopic.value.id)
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const removeReply = async (reply) => {
  try {
    await ElMessageBox.confirm('确认删除该回复吗？', '提示', { type: 'warning' })
    await http.del(`/api/discussions/replies/${reply.id}`)
    ElMessage.success('删除成功')
    fetchReplies(currentTopic.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const filteredTopics = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) {
    return topics.value
  }
  return topics.value.filter((topic) => topic.title.toLowerCase().includes(keyword))
})

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

onMounted(fetchCourses)
</script>

<style scoped lang="scss">
.discussion-page {
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
  gap: 8px;
}

.detail-card {
  min-height: 640px;
}

.topic-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.topic-meta {
  display: flex;
  gap: 16px;
  color: #94a3b8;
  font-size: 12px;
}

.topic-content {
  white-space: pre-wrap;
  color: #1e293b;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.empty-topic {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 480px;
  color: #94a3b8;
}
</style>
