
export type UserRole = 'ROLE_ADMIN' | 'ROLE_TEACHER' | 'ROLE_STUDENT';

export interface User {
  id: number;
  username: string;
  email: string;
  studentNumber?: string;
  className?: string;
  role: UserRole;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
}

export interface DashboardSummary {
  totalStudents: number;
  totalCourses: number;
  assignmentSubmissions: number;
  quizSubmissions: number;
}

export interface Course {
  id: number;
  title: string;
  description: string;
  syllabus?: string;
  teacherId?: number;
  teacherName?: string;
  thumbnail?: string; // 适配前端展示
  enrolled?: number;
  category?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TeacherCard {
  id: number;
  name: string;
  avatar: string;
  courses: number;
}

export interface DiscussionTopic {
  id: number;
  courseId: number;
  authorId: number;
  authorUsername: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt?: string;
}

export interface DiscussionReply {
  id: number;
  topicId: number;
  authorId: number;
  authorUsername: string;
  content: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success?: boolean;
}

export type AppView = 'dashboard' | 'courses' | 'teachers' | 'discussions' | 'profile' | 'students';

export interface HistoryItem {
  id: string;
  originalImage: string;
  editedImage: string;
  prompt: string;
  timestamp: number;
}
