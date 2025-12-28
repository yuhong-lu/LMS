import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import CourseList from '../views/CourseList.vue'
import Students from '../views/Students.vue'
import Login from '../views/Login.vue'
import CourseResources from '../views/CourseResources.vue'
import TeacherCenter from '../views/TeacherCenter.vue'
import Discussions from '../views/Discussions.vue'
import Register from '../views/Register.vue'
import AdminCenter from '../views/AdminCenter.vue'
import Profile from '../views/Profile.vue'
import { getUser, isAuthenticated } from '../services/auth'

const routes = [
  {
    path: '/login',
    component: Login,
  },
  {
    path: '/register',
    component: Register,
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'courses', component: CourseList },
      { path: 'courses/:courseId/resources', component: CourseResources },
      { path: 'teacher', component: TeacherCenter, meta: { roles: ['ROLE_TEACHER', 'ROLE_ADMIN'] } },
      { path: 'students', component: Students, meta: { roles: ['ROLE_STUDENT'] } },
      { path: 'admin', component: AdminCenter, meta: { roles: ['ROLE_ADMIN'] } },
      { path: 'discussions', component: Discussions },
      { path: 'profile', component: Profile },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.path === '/login' || to.path === '/register') {
    if (isAuthenticated()) {
      return '/dashboard'
    }
    return true
  }
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return '/login'
  }
  if (to.meta?.roles) {
    const user = getUser()
    const roles = user?.roles || []
    const allowed = to.meta.roles.some((role) => roles.includes(role))
    if (!allowed) {
      return '/dashboard'
    }
  }
  return true
})

export default router
