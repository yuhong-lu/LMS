<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="240px" class="aside-menu">
        <div class="logo">
          <div class="logo-mark">EF</div>
          <div>
            <div class="logo-title">EduFlow</div>
            <div class="logo-sub">Learning Suite</div>
          </div>
        </div>
        <div class="aside-scroll">
          <el-menu
            :default-active="$route.path"
            class="el-menu-vertical"
            background-color="transparent"
            text-color="#6b7280"
            active-text-color="#6366f1"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/courses">
              <el-icon><Reading /></el-icon>
              <span>课程管理</span>
            </el-menu-item>
            <el-menu-item v-if="isTeacher" index="/teacher">
              <el-icon><School /></el-icon>
              <span>教师中心</span>
            </el-menu-item>
            <el-menu-item v-if="isStudent" index="/students">
              <el-icon><User /></el-icon>
              <span>学员中心</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin">
              <el-icon><UserFilled /></el-icon>
              <span>管理员</span>
            </el-menu-item>
            <el-menu-item index="/discussions">
              <el-icon><ChatLineRound /></el-icon>
              <span>讨论区</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="aside-footer">
          <el-dropdown trigger="click" @command="handleFooterCommand">
            <div class="footer-top">
              <div class="footer-avatar">
                <el-avatar size="small" src="https://api.dicebear.com/7.x/avataaars/svg?seed=EduFlow" />
              </div>
              <div class="footer-text">
                <div class="name">{{ username }}</div>
                <div class="role">{{ roles[0] || '用户' }}</div>
              </div>
              <el-icon class="footer-arrow"><ArrowRight /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-aside>

      <el-container>
        <el-header class="main-header">
          <div class="header-left">
            <div class="pill">
              <el-icon><Odometer /></el-icon>
              智慧教务 · EduFlow
            </div>
          </div>
          <div class="header-right"></div>
        </el-header>

        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession, getUser } from '../services/auth'

const router = useRouter()
const user = ref(getUser())
const username = computed(() => user.value?.username || 'User')
const roles = computed(() => user.value?.roles || [])
const isTeacher = computed(
  () => roles.value.includes('ROLE_TEACHER') || roles.value.includes('ROLE_ADMIN'),
)
const isStudent = computed(() => roles.value.includes('ROLE_STUDENT'))
const isAdmin = computed(() => roles.value.includes('ROLE_ADMIN'))

const goProfile = () => {
  router.push('/profile')
}

const logout = () => {
  clearSession()
  router.replace('/login')
}

const handleFooterCommand = (command) => {
  if (command === 'profile') {
    goProfile()
  } else if (command === 'logout') {
    logout()
  }
}
</script>

<style scoped lang="scss">
.common-layout,
.el-container {
  height: 100vh;
}

.aside-menu {
  background: linear-gradient(180deg, #f5f7ff 0%, #eef2ff 100%);
  color: #0f172a;
  border-right: 1px solid #e6e8f7;
  display: flex;
  flex-direction: column;
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 72px;
    padding: 18px 20px;
    font-weight: 700;
    color: #111827;
    .logo-mark {
      width: 42px;
      height: 42px;
      border-radius: 16px;
      display: grid;
      place-items: center;
      font-weight: 800;
      color: #fff;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      box-shadow: 0 10px 24px rgba(99, 102, 241, 0.25);
    }
    .logo-title {
      font-size: 16px;
      line-height: 1.1;
    }
    .logo-sub {
      font-size: 12px;
      color: #6b7280;
    }
  }
  .aside-scroll {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 12px;
  }
  :deep(.el-menu) {
    border-right: none;
    padding: 8px 12px 16px 12px;
  }
  :deep(.el-menu-item) {
    height: 46px;
    margin-bottom: 6px;
    border-radius: 14px;
    font-weight: 600;
  }
  :deep(.el-menu-item.is-active) {
    background: #fff;
    box-shadow: 0 10px 22px rgba(99, 102, 241, 0.08);
    border: 1px solid #e6e8f7;
  }
  :deep(.el-menu-item:hover) {
    color: #4f46e5 !important;
  }
  .aside-footer {
    margin: 12px;
    padding: 12px;
    border-radius: 14px;
    border: 1px solid #e6e8f7;
    background: #fff;
    display: flex;
    flex-direction: column;
    gap: 10px;
    transition: all 0.2s ease;
    box-shadow: 0 10px 22px rgba(15, 23, 42, 0.05);
    .footer-top {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      &:hover {
        transform: translateY(-2px);
      }
      .footer-avatar {
        width: 36px;
        height: 36px;
        border-radius: 12px;
        background: #eef2ff;
        display: grid;
        place-items: center;
      }
      .footer-text {
        flex: 1;
        line-height: 1.1;
        .name {
          font-weight: 700;
          color: #0f172a;
        }
        .role {
          font-size: 12px;
          color: #6b7280;
        }
      }
      .footer-arrow {
        color: #c7cce9;
      }
    }
  }
}

.main-header {
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e8f7;
  box-shadow: 0 12px 34px rgba(15, 23, 42, 0.04);
  padding: 16px 22px;

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      gap: 8px;
      .user-meta {
        display: flex;
        flex-direction: column;
        line-height: 1.1;
        .username {
          font-weight: 700;
          color: #0f172a;
        }
        .user-role {
          font-size: 12px;
          color: #6b7280;
        }
      }
    }
  }
}

.main-content {
  background-color: #f7f8ff;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
