
import {
  Course,
  User,
  DashboardSummary,
  AuthResponse,
  DiscussionTopic,
  DiscussionReply,
} from '../types';

// 注意：在本地开发环境下，配合 vite.config.ts 的 proxy 配置
// 这里的 BASE_URL 设为 '/api' 即可自动转发到 localhost:8080
const BASE_URL = '/api'; 

const getHeaders = () => {
  const token = localStorage.getItem('lms_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
  };
};

const handleResponse = async <T>(response: Response) => {
  if (response.status === 401 || response.status === 403) {
    const isLoginPage = window.location.pathname.includes('login');
    if (!isLoginPage) {
      localStorage.removeItem('lms_token');
      window.location.reload();
    }
  }

  let data: any = null;
  try {
    data = await response.json();
  } catch (e) {
    // 非 JSON 响应，忽略解析错误
  }

  if (!response.ok) {
    const msg = data?.message || response.statusText || '网络请求错误';
    throw new Error(msg);
  }
  return (data as T) ?? ({} as T);
};

export const apiService = {
  async login(credentials: any): Promise<AuthResponse> {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });
    return handleResponse<AuthResponse>(res);
  },

  async register(payload: any): Promise<AuthResponse> {
    const res = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    return handleResponse<AuthResponse>(res);
  },

  async getMe(): Promise<User> {
    const res = await fetch(`${BASE_URL}/users/me`, { headers: getHeaders() });
    const raw = await handleResponse<any>(res);
    const role =
      Array.isArray(raw?.authorities) && raw.authorities.length > 0
        ? raw.authorities[0].authority
        : raw?.role || 'ROLE_STUDENT';
    let user: User = {
      id: raw.id ?? 0,
      username: raw.username ?? '',
      email: raw.email ?? '',
      studentNumber: raw.studentNumber,
      className: raw.className,
      role,
      createdAt: raw.createdAt ?? '',
    };
    // 信息不完整时，尝试从 admin/users 填充（仅管理员）
    if (role === 'ROLE_ADMIN' && (!user.email || user.id === 0)) {
      try {
        const list = await this.listUsers();
        const found = list.find((u) => u.username === user.username);
        if (found) {
          user = { ...user, ...found };
        }
      } catch (e) {
        // 忽略填充失败
      }
    }
    return user;
  },

  async getSummary(): Promise<DashboardSummary> {
    const res = await fetch(`${BASE_URL}/dashboard/summary`, { headers: getHeaders() });
    return handleResponse<DashboardSummary>(res);
  },

  async getCourses(): Promise<Course[]> {
    const res = await fetch(`${BASE_URL}/courses`, { headers: getHeaders() });
    return handleResponse<Course[]>(res);
  },

  async getCourseResources(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/courses/${courseId}/resources`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async addCourseResource(courseId: number, payload: { title: string; type: string; url?: string; content?: string }): Promise<any> {
    const res = await fetch(`${BASE_URL}/courses/${courseId}/resources`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<any>(res);
  },

  async deleteCourseResource(resourceId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/courses/resources/${resourceId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    await handleResponse(res);
  },

  async listAssignments(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/assignments/courses/${courseId}`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async createAssignment(courseId: number, payload: any): Promise<any> {
    const res = await fetch(`${BASE_URL}/assignments/courses/${courseId}`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<any>(res);
  },

  async deleteAssignment(assignmentId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/assignments/${assignmentId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    await handleResponse(res);
  },

  async listAssignmentSubmissions(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/assignments/courses/${courseId}/submissions`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async listQuizzes(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/quizzes/courses/${courseId}`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async createQuiz(courseId: number, payload: any): Promise<any> {
    const res = await fetch(`${BASE_URL}/quizzes/courses/${courseId}`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<any>(res);
  },

  async deleteQuiz(quizId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/quizzes/${quizId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    await handleResponse(res);
  },

  async listQuizSubmissions(quizId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/quizzes/${quizId}/submissions`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async getCourseGradeSummary(courseId: number): Promise<any> {
    const res = await fetch(`${BASE_URL}/grades/courses/${courseId}/summary`, { headers: getHeaders() });
    return handleResponse<any>(res);
  },

  async listCourseStudentGrades(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/grades/courses/${courseId}/students`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async getTopics(courseId: string | number): Promise<DiscussionTopic[]> {
    const res = await fetch(`${BASE_URL}/discussions/courses/${courseId}/topics`, { headers: getHeaders() });
    return handleResponse<DiscussionTopic[]>(res);
  },

  async createTopic(courseId: string | number, payload: { title: string; content: string }): Promise<DiscussionTopic> {
    const res = await fetch(`${BASE_URL}/discussions/courses/${courseId}/topics`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<DiscussionTopic>(res);
  },

  async getReplies(topicId: number): Promise<DiscussionReply[]> {
    const res = await fetch(`${BASE_URL}/discussions/topics/${topicId}/replies`, { headers: getHeaders() });
    return handleResponse<DiscussionReply[]>(res);
  },

  async addReply(topicId: number, payload: { content: string }): Promise<DiscussionReply> {
    const res = await fetch(`${BASE_URL}/discussions/topics/${topicId}/replies`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<DiscussionReply>(res);
  },

  async listUsers(): Promise<User[]> {
    const res = await fetch(`${BASE_URL}/admin/users`, { headers: getHeaders() });
    return handleResponse<User[]>(res);
  },

  async listStudents(): Promise<User[]> {
    const res = await fetch(`${BASE_URL}/users/students`, { headers: getHeaders() });
    return handleResponse<User[]>(res);
  },

  async listEnrollments(courseId: number): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/enrollments/courses/${courseId}`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async listMyEnrollments(): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/enrollments/my`, { headers: getHeaders() });
    return handleResponse<any[]>(res);
  },

  async uploadFile(file: File, courseId?: number): Promise<{ url: string; name: string }> {
    const formData = new FormData();
    formData.append('file', file);
    if (courseId) formData.append('courseId', String(courseId));
    const token = localStorage.getItem('lms_token');
    const res = await fetch(`${BASE_URL}/uploads`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        // 让浏览器自动设置 multipart boundary
      } as any,
      body: formData,
    });
    return handleResponse(res);
  },

  async assignEnrollment(payload: { courseId: number; studentId: number }): Promise<void> {
    const res = await fetch(`${BASE_URL}/enrollments`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    await handleResponse(res);
  },

  async removeEnrollment(enrollmentId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/enrollments/${enrollmentId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    await handleResponse(res);
  },

  async createCourse(payload: { title: string; description?: string; syllabus?: string; teacherId: number }): Promise<Course> {
    const res = await fetch(`${BASE_URL}/courses`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return handleResponse<Course>(res);
  },
};
