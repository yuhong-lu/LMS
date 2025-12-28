<template>
  <div class="resource-page eduflow-page">
    <div class="header">
      <div>
        <div class="title">课程资源</div>
        <div class="subtitle">{{ courseTitle || '加载中...' }}</div>
      </div>
      <div class="actions" v-if="canEdit">
        <el-button type="primary" size="large" @click="openCreate">新增资源</el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="resources" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="previewResource(row)">查看</el-button>
            <el-button size="small" type="primary" plain v-if="canEdit" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" plain v-if="canEdit" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="540px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option label="文本" value="TEXT" />
            <el-option label="音频" value="AUDIO" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="文件" value="FILE" />
            <el-option label="链接" value="LINK" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="form.url" placeholder="可选，资源链接或文件地址" />
          <el-upload
            class="upload"
            drag
            :show-file-list="false"
            :http-request="uploadFile"
            :disabled="uploading"
          >
            <div class="upload-drag">
              <div class="upload-title">拖拽文件到此处上传</div>
              <el-button size="small" :loading="uploading">或点击选择文件</el-button>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" rows="4" placeholder="可选，文本内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="资源预览" width="600px">
      <div class="preview">
        <div class="preview-row">
          <span class="label">标题：</span>
          <span>{{ previewItem?.title }}</span>
        </div>
        <div class="preview-row">
          <span class="label">类型：</span>
          <span>{{ previewItem?.type }}</span>
        </div>
        <div class="preview-row" v-if="previewItem?.url">
          <span class="label">链接：</span>
          <a :href="previewItem.url" target="_blank" rel="noreferrer">{{ previewItem.url }}</a>
        </div>
        <div class="preview-row" v-if="previewItem?.type === 'AUDIO' && previewItem?.url">
          <audio controls :src="previewItem.url"></audio>
        </div>
        <div class="preview-row" v-if="previewItem?.type === 'VIDEO' && previewItem?.url">
          <video controls :src="previewItem.url" style="max-width: 100%"></video>
        </div>
        <div class="preview-row" v-if="previewItem?.content">
          <span class="label">内容：</span>
          <div class="content">{{ previewItem.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../services/http'
import { getToken, getUser } from '../services/auth'

const route = useRoute()
const courseId = Number(route.params.courseId)
const resources = ref([])
const loading = ref(false)
const courseTitle = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('新增资源')
const previewVisible = ref(false)
const previewItem = ref(null)
const editingId = ref(null)
const uploading = ref(false)

const form = ref({
  type: 'TEXT',
  title: '',
  url: '',
  content: '',
})

const user = getUser()
const canEdit = computed(() => {
  const roles = user?.roles || []
  return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_TEACHER')
})

const fetchCourse = async () => {
  try {
    const data = await http.get(`/api/courses/${courseId}`)
    courseTitle.value = data.title
  } catch (error) {
    courseTitle.value = ''
  }
}

const fetchResources = async () => {
  loading.value = true
  try {
    const data = await http.get(`/api/courses/${courseId}/resources`)
    resources.value = (data || []).map((item) => ({
      ...item,
      createdAt: item.createdAt ? new Date(item.createdAt).toLocaleString() : '',
    }))
  } catch (error) {
    ElMessage.error('资源加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增资源'
  editingId.value = null
  form.value = {
    type: 'TEXT',
    title: '',
    url: '',
    content: '',
  }
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑资源'
  editingId.value = row.id
  form.value = {
    type: row.type || 'TEXT',
    title: row.title || '',
    url: row.url || '',
    content: row.content || '',
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.title) {
    ElMessage.warning('请填写标题')
    return
  }
  try {
    if (editingId.value) {
      await http.put(`/api/courses/resources/${editingId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await http.post(`/api/courses/${courseId}/resources`, form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchResources()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const uploadFile = async (options) => {
  const { file } = options
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const token = getToken()
    formData.append('courseId', String(courseId))
    const response = await fetch('/api/uploads', {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    })
    if (!response.ok) {
      throw new Error('Upload failed')
    }
    const data = await response.json()
    form.value.url = data.url
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除资源「${row.title}」吗？`, '提示', {
      type: 'warning',
    })
    await http.del(`/api/courses/resources/${row.id}`)
    ElMessage.success('删除成功')
    fetchResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const previewResource = (row) => {
  previewItem.value = row
  previewVisible.value = true
}

onMounted(() => {
  fetchCourse()
  fetchResources()
})
</script>

<style scoped lang="scss">
.resource-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
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

.upload {
  margin-top: 10px;
}

.upload-drag {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 12px;
  color: #64748b;
}

.upload-title {
  font-size: 13px;
}

.label {
  width: 60px;
  color: #94a3b8;
}

.content {
  white-space: pre-wrap;
  color: #0f172a;
}
</style>
