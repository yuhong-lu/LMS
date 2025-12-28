<template>
  <div class="dashboard eduflow-page">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in stats" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: item.color }">
            <component :is="item.icon" />
          </div>
          <div class="stat-info">
            <div class="label">{{ item.label }}</div>
            <div class="value">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>学习趋势分析</span>
              <el-button text>查看详情</el-button>
            </div>
          </template>
          <div
            style="height: 300px; background: #f1f5f9; display: flex; align-items: center; justify-content: center; color: #94a3b8;"
          >
            此处集成 ECharts 图表组件
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>待办事项</span>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2023/10/01" placement="top" type="primary">
              发布新的 Python 进阶课程
            </el-timeline-item>
            <el-timeline-item timestamp="2023/10/03" placement="top" type="success">
              审核 50 位新学员注册
            </el-timeline-item>
            <el-timeline-item timestamp="2023/10/05" placement="top">
              系统定期维护
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, VideoPlay, Trophy, Money } from '@element-plus/icons-vue'
import { http } from '../services/http'

const summary = ref({
  totalStudents: 0,
  totalCourses: 0,
  assignmentSubmissions: 0,
  quizSubmissions: 0,
})

const stats = computed(() => [
  {
    label: '总学员数',
    value: summary.value.totalStudents,
    icon: User,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    label: '在线课程',
    value: summary.value.totalCourses,
    icon: VideoPlay,
    color: 'linear-gradient(135deg, #2af598 0%, #009efd 100%)',
  },
  {
    label: '作业提交',
    value: summary.value.assignmentSubmissions,
    icon: Trophy,
    color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)',
  },
  {
    label: '测验提交',
    value: summary.value.quizSubmissions,
    icon: Money,
    color: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  },
])

const fetchSummary = async () => {
  try {
    const data = await http.get('/api/dashboard/summary')
    summary.value = {
      totalStudents: data.totalStudents ?? 0,
      totalCourses: data.totalCourses ?? 0,
      assignmentSubmissions: data.assignmentSubmissions ?? 0,
      quizSubmissions: data.quizSubmissions ?? 0,
    }
  } catch (error) {
    ElMessage.error('仪表盘数据加载失败')
  }
}

onMounted(fetchSummary)
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stat-card {
  border: 1px solid var(--ef-border);
  border-radius: 20px;
  box-shadow: var(--ef-shadow);
  background: #fff;
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    padding: 20px 18px;
  }
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 15px;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.15);
}

.stat-info {
  .label {
    font-size: 14px;
    color: #64748b;
  }
  .value {
    font-size: 24px;
    font-weight: bold;
    color: #0f172a;
    margin-top: 4px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
