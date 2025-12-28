<template>
  <div class="course-list eduflow-page">
    <div class="toolbar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索课程名称..."
        :prefix-icon="Search"
        style="width: 300px"
        size="large"
      />
      <div class="actions">
        <el-radio-group v-model="filterType" size="large">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="published">已发布</el-radio-button>
          <el-radio-button label="draft">草稿</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-row :gutter="24" class="course-grid">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in filteredCourses" :key="course.id">
        <el-card :body-style="{ padding: '0px' }" shadow="hover" class="course-card">
          <div class="image-wrapper">
            <img :src="course.cover" class="image" />
            <div class="tag success">已发布</div>
          </div>
          <div class="content">
            <h3 class="title text-ellipsis">{{ course.title }}</h3>
            <p class="desc text-ellipsis-2">{{ course.desc }}</p>

            <div class="meta">
              <span><el-icon><User /></el-icon> {{ course.teacher || '未分配' }}</span>
              <span class="price free">课程</span>
            </div>

            <div class="footer">
              <div class="btn-group">
                <el-button size="small" @click="goToResources(course.id)">查看</el-button>
                <el-button size="small" type="primary" plain @click="goToResources(course.id)">
                  资源
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, User } from '@element-plus/icons-vue'
import { http } from '../services/http'

const searchQuery = ref('')
const filterType = ref('all')
const courses = ref([])
const router = useRouter()

const covers = [
  'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=500&q=60',
  'https://images.unsplash.com/photo-1561070791-2526d30994b5?auto=format&fit=crop&w=500&q=60',
  'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=500&q=60',
  'https://images.unsplash.com/photo-1627398242454-45a1465c2479?auto=format&fit=crop&w=500&q=60',
]

const fetchCourses = async () => {
  try {
    const data = await http.get('/api/courses')
    courses.value = (data || []).map((course, index) => ({
      id: course.id,
      title: course.title,
      desc: course.description || course.syllabus || '暂无课程简介',
      teacher: course.teacherUsername,
      cover: covers[index % covers.length],
    }))
  } catch (error) {
    ElMessage.error('课程加载失败')
  }
}

const filteredCourses = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) {
    return courses.value
  }
  return courses.value.filter((course) => course.title.toLowerCase().includes(keyword))
})

const goToResources = (courseId) => {
  router.push(`/courses/${courseId}/resources`)
}

onMounted(fetchCourses)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.actions {
  display: flex;
  gap: 16px;
}

.course-card {
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
  }

  .image-wrapper {
    position: relative;
    height: 160px;
    overflow: hidden;

    .image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }

    .tag {
      position: absolute;
      top: 10px;
      right: 10px;
      padding: 4px 12px;
      border-radius: 20px;
      color: white;
      font-size: 12px;
      font-weight: bold;
      &.success {
        background: rgba(103, 194, 58, 0.9);
      }
      &.warning {
        background: rgba(230, 162, 60, 0.9);
      }
    }
  }

  &:hover .image {
    transform: scale(1.1);
  }

  .content {
    padding: 16px;

    .title {
      font-size: 16px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px 0;
    }

    .desc {
      font-size: 13px;
      color: #64748b;
      line-height: 1.5;
      height: 40px;
      margin-bottom: 16px;
    }

    .meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #94a3b8;
      margin-bottom: 16px;

      .price {
        font-size: 16px;
        color: #f56c6c;
        font-weight: bold;
        &.free {
          color: #67c23a;
        }
      }
    }

    .footer {
      border-top: 1px solid #f1f5f9;
      padding-top: 12px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
