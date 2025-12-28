import React, { useEffect, useMemo, useRef, useState } from 'react';
import { apiService } from '../services/apiService';
import { Course, User } from '../types';

interface Props {
  currentUser: User | null;
}

const TeacherCenter: React.FC<Props> = ({ currentUser }) => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [students, setStudents] = useState<User[]>([]);
  const [resources, setResources] = useState<any[]>([]);
  const [assignments, setAssignments] = useState<any[]>([]);
  const [quizzes, setQuizzes] = useState<any[]>([]);
  const [enrollments, setEnrollments] = useState<any[]>([]);
  const [assignmentSubmissions, setAssignmentSubmissions] = useState<any[]>([]);
  const [quizSubmissions, setQuizSubmissions] = useState<Record<number, any[]>>({});
  const [gradeSummary, setGradeSummary] = useState<any | null>(null);
  const [studentGrades, setStudentGrades] = useState<any[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'resources' | 'assignments' | 'quizzes' | 'enrollments' | 'grades'>('resources');
  const [uploading, setUploading] = useState(false);
  const [newResource, setNewResource] = useState({ title: '', type: 'FILE', url: '', content: '' });
  const [newAssignment, setNewAssignment] = useState({ title: '', description: '', dueAt: '' });
  const [newQuiz, setNewQuiz] = useState({ title: '', description: '', startAt: '', endAt: '' });
  const [selectedStudentId, setSelectedStudentId] = useState<number | ''>('');
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const data = await apiService.getCourses();
        setCourses(data);
        if (data.length) {
          setSelectedCourseId(data[0].id);
        }
        const users = await apiService.listStudents();
        setStudents(users);
      } catch (err: any) {
        setError(err.message || '课程加载失败');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    const loadDetail = async () => {
      if (!selectedCourseId) return;
      const course = courses.find((c) => c.id === selectedCourseId);
      if (!course) return;
      setDetailError(null);
      setDetailLoading(true);
      setActiveTab('resources');
      setQuizSubmissions({});
      try {
        const [resList, assignList, quizList, enrollList, assignSubs, gradeSum, studentSum] = await Promise.all([
          apiService.getCourseResources(course.id),
          apiService.listAssignments(course.id),
          apiService.listQuizzes(course.id),
          apiService.listEnrollments(course.id),
          apiService.listAssignmentSubmissions(course.id),
          apiService.getCourseGradeSummary(course.id),
          apiService.listCourseStudentGrades(course.id),
        ]);
        setResources(resList);
        setAssignments(assignList);
        setQuizzes(quizList);
        setEnrollments(enrollList);
        setAssignmentSubmissions(assignSubs);
        setGradeSummary(gradeSum);
        setStudentGrades(studentSum);
      } catch (err: any) {
        setDetailError(err.message || '详情加载失败');
      } finally {
        setDetailLoading(false);
      }
    };
    loadDetail();
  }, [selectedCourseId, courses]);

  const selectedCourse = useMemo(
    () => courses.find((c) => c.id === selectedCourseId) || null,
    [courses, selectedCourseId],
  );

  const handleCourseChange = (value: number) => {
    setSelectedCourseId(value);
  };

  const resolveUrl = (url?: string) => {
    if (!url) return '';
    return url.startsWith('http') ? url : `${(import.meta as any).env?.VITE_FILE_BASE || 'http://localhost:8080'}${url}`;
  };

  const handleResourceFileSelect = async (files?: FileList | null) => {
    if (!files || !files.length || !selectedCourseId) return;
    const file = files[0];
    try {
      setUploading(true);
      const result = await apiService.uploadFile(file, selectedCourseId);
      setNewResource((prev) => ({
        ...prev,
        url: result.url,
        title: prev.title || file.name,
        type: 'FILE',
      }));
    } catch (err: any) {
      setDetailError(err.message || '上传失败');
    } finally {
      setUploading(false);
    }
  };

  const handleAddResource = async () => {
    if (!selectedCourseId || !newResource.title) {
      setDetailError('请填写资源标题');
      return;
    }
    try {
      const created = await apiService.addCourseResource(selectedCourseId, newResource);
      setResources((prev) => [created, ...prev]);
      setNewResource({ title: '', type: 'FILE', url: '', content: '' });
    } catch (err: any) {
      setDetailError(err.message || '新增资源失败');
    }
  };

  const handleDeleteResource = async (id: number) => {
    try {
      await apiService.deleteCourseResource(id);
      setResources((prev) => prev.filter((r) => r.id !== id));
    } catch (err: any) {
      setDetailError(err.message || '删除资源失败');
    }
  };

  const handleCreateAssignment = async () => {
    if (!selectedCourseId || !newAssignment.title) {
      setDetailError('请填写作业标题');
      return;
    }
    try {
      const created = await apiService.createAssignment(selectedCourseId, newAssignment);
      setAssignments((prev) => [created, ...prev]);
      setNewAssignment({ title: '', description: '', dueAt: '' });
    } catch (err: any) {
      setDetailError(err.message || '创建作业失败');
    }
  };

  const handleDeleteAssignment = async (id: number) => {
    try {
      await apiService.deleteAssignment(id);
      setAssignments((prev) => prev.filter((a) => a.id !== id));
    } catch (err: any) {
      setDetailError(err.message || '删除作业失败');
    }
  };

  const handleCreateQuiz = async () => {
    if (!selectedCourseId || !newQuiz.title) {
      setDetailError('请填写测验标题');
      return;
    }
    try {
      const created = await apiService.createQuiz(selectedCourseId, newQuiz);
      setQuizzes((prev) => [created, ...prev]);
      setNewQuiz({ title: '', description: '', startAt: '', endAt: '' });
    } catch (err: any) {
      setDetailError(err.message || '创建测验失败');
    }
  };

  const handleDeleteQuiz = async (id: number) => {
    try {
      await apiService.deleteQuiz(id);
      setQuizzes((prev) => prev.filter((q) => q.id !== id));
    } catch (err: any) {
      setDetailError(err.message || '删除测验失败');
    }
  };

  const handleAssignEnrollment = async () => {
    if (!selectedCourseId || !selectedStudentId) {
      setDetailError('请选择学生');
      return;
    }
    try {
      await apiService.assignEnrollment({ courseId: selectedCourseId, studentId: Number(selectedStudentId) });
      const list = await apiService.listEnrollments(selectedCourseId);
      setEnrollments(list);
      setSelectedStudentId('');
    } catch (err: any) {
      setDetailError(err.message || '分配失败');
    }
  };

  const refreshQuizSubmissions = async (quizId: number) => {
    setDetailError(null);
    try {
      const list = await apiService.listQuizSubmissions(quizId);
      setQuizSubmissions((prev) => ({ ...prev, [quizId]: list }));
    } catch (err: any) {
      setDetailError(err.message || '测验提交加载失败');
    }
  };

  return (
    <div className="p-6 md:p-10">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-lg font-bold text-slate-800">教师中心</h1>
          <p className="text-slate-400 text-sm">
            {currentUser?.role === 'ROLE_ADMIN' ? '查看全部课程' : '我的授课列表'}
          </p>
        </div>
      </div>

      {error && (
        <div className="mb-4 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-4">
          {error}
        </div>
      )}

      <div className="bg-white border border-slate-100 rounded-2xl shadow-sm p-4 mb-4 flex flex-col gap-3">
        <div className="flex flex-col sm:flex-row sm:items-center gap-3 justify-between">
          <div>
            <div className="text-sm font-bold text-slate-800">选择课程</div>
            <div className="text-[11px] text-slate-500">查看并管理该课程的资源、作业、测验、注册与成绩</div>
          </div>
          <select
            className="px-3 py-2 border border-slate-200 rounded-lg text-sm min-w-[220px]"
            value={selectedCourseId || ''}
            onChange={(e) => handleCourseChange(Number(e.target.value))}
          >
            {courses.map((c) => (
              <option key={c.id} value={c.id}>{c.title}</option>
            ))}
          </select>
        </div>
        {selectedCourse && (
          <div className="flex flex-wrap gap-3 text-xs text-slate-500">
            <span>讲师：{selectedCourse.teacherName || `#${selectedCourse.teacherId || '-'}`}</span>
            <span>创建：{selectedCourse.createdAt ? new Date(selectedCourse.createdAt).toLocaleDateString() : '—'}</span>
            <span>资源：{resources.length}</span>
            <span>作业：{assignments.length}</span>
            <span>测验：{quizzes.length}</span>
            <span>选课：{enrollments.length}</span>
          </div>
        )}
      </div>

      <div className="flex flex-wrap gap-2 mb-3">
        {[
          { id: 'resources', label: '资源' },
          { id: 'assignments', label: '作业' },
          { id: 'quizzes', label: '测验' },
          { id: 'enrollments', label: '选课' },
          { id: 'grades', label: '成绩' },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as typeof activeTab)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
              activeTab === tab.id
                ? 'border-indigo-200 bg-indigo-50 text-indigo-600'
                : 'border-slate-200 bg-white text-slate-600 hover:border-indigo-100'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {detailError && (
        <div className="mb-3 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-3">
          {detailError}
        </div>
      )}

      {activeTab === 'resources' && (
        <section className="border border-slate-100 rounded-xl p-4 bg-white shadow-sm">
          <div className="text-sm font-bold text-slate-800 mb-2 flex items-center justify-between">
            <span>课程资源</span>
            {detailLoading && <span className="text-[11px] text-slate-400">加载中...</span>}
          </div>
          <div className="flex flex-col lg:flex-row lg:items-center gap-2 mb-3">
            <input
              className="flex-1 px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="资源标题"
              value={newResource.title}
              onChange={(e) => setNewResource({ ...newResource, title: e.target.value })}
            />
            <input
              className="flex-1 px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="链接/文件URL（可选）"
              value={newResource.url}
              onChange={(e) => setNewResource({ ...newResource, url: e.target.value })}
            />
            <button
              className="px-4 py-2 gradient-bg text-white rounded-lg text-sm"
              onClick={handleAddResource}
            >
              新增资源
            </button>
          </div>
          <div
            className="mb-3 border-2 border-dashed border-indigo-200 rounded-2xl bg-indigo-50/60 text-indigo-500 text-sm flex flex-col items-center justify-center py-8 px-4 cursor-pointer"
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => {
              e.preventDefault();
              handleResourceFileSelect(e.dataTransfer.files);
            }}
            onClick={() => fileInputRef.current?.click()}
          >
            <i className="fa-solid fa-cloud-arrow-up text-xl mb-2"></i>
            <div className="font-semibold">{uploading ? '上传中...' : '拖拽文件到此或点击选择'}</div>
            <div className="text-[11px] text-indigo-400 mt-1">支持课件、音视频等（自动填充标题/URL）</div>
            <input
              type="file"
              ref={fileInputRef}
              className="hidden"
              onChange={(e) => handleResourceFileSelect(e.target.files)}
            />
          </div>
          <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
            {resources.length === 0 && <div className="text-slate-400 text-sm">暂无资源</div>}
            {resources.map((r) => (
              <div key={r.id} className="p-3 rounded-lg border border-slate-100 flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-slate-800">{r.title}</div>
                  <div className="text-[11px] text-indigo-500">{r.type}</div>
                </div>
                {r.url && (
                  <a className="text-xs text-indigo-500" href={r.url} target="_blank" rel="noreferrer">
                    查看
                  </a>
                )}
                <button
                  className="text-[11px] text-rose-500 ml-3"
                  onClick={() => handleDeleteResource(r.id)}
                >
                  删除
                </button>
              </div>
            ))}
          </div>
        </section>
      )}

      {activeTab === 'assignments' && (
        <section className="border border-slate-100 rounded-xl p-4 bg-white shadow-sm">
          <div className="text-sm font-bold text-slate-800 mb-2 flex items-center justify-between">
            <span>作业与提交</span>
            {detailLoading && <span className="text-[11px] text-slate-400">加载中...</span>}
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-2 mb-3">
            <input
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="作业标题"
              value={newAssignment.title}
              onChange={(e) => setNewAssignment({ ...newAssignment, title: e.target.value })}
            />
            <input
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="截止时间 YYYY-MM-DD HH:mm"
              value={newAssignment.dueAt}
              onChange={(e) => setNewAssignment({ ...newAssignment, dueAt: e.target.value })}
            />
            <div className="flex gap-2">
              <button
                className="px-4 py-2 gradient-bg text-white rounded-lg text-sm"
                onClick={handleCreateAssignment}
              >
                创建作业
              </button>
            </div>
          </div>
          <textarea
            className="w-full mb-3 px-3 py-2 border border-slate-200 rounded-lg text-sm"
            placeholder="作业描述（可选）"
            value={newAssignment.description}
            onChange={(e) => setNewAssignment({ ...newAssignment, description: e.target.value })}
          />
          <div className="space-y-3 max-h-[500px] overflow-y-auto pr-1">
            {assignments.length === 0 && <div className="text-slate-400 text-sm">暂无作业</div>}
            {assignments.map((a) => {
              const subs = assignmentSubmissions.filter((s) => s.assignmentId === a.id);
              return (
                <div key={a.id} className="p-3 rounded-lg border border-slate-100">
                  <div className="flex items-center justify-between">
                    <div className="text-sm font-semibold text-slate-800">{a.title}</div>
                    <div className="text-[11px] text-slate-500">
                      截止：{a.dueAt ? new Date(a.dueAt).toLocaleString() : '未设置'}
                    </div>
                  </div>
                  <div className="text-[11px] text-slate-500 mt-1">提交：{subs.length} 条</div>
                  <button
                    className="mt-2 text-[11px] text-rose-500"
                    onClick={() => handleDeleteAssignment(a.id)}
                  >
                    删除作业
                  </button>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {activeTab === 'quizzes' && (
        <section className="border border-slate-100 rounded-xl p-4 bg-white shadow-sm">
          <div className="text-sm font-bold text-slate-800 mb-2 flex items-center justify-between">
            <span>测验与提交</span>
            {detailLoading && <span className="text-[11px] text-slate-400">加载中...</span>}
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2 mb-3">
            <input
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="测验标题"
              value={newQuiz.title}
              onChange={(e) => setNewQuiz({ ...newQuiz, title: e.target.value })}
            />
            <input
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="时间窗 开始 YYYY-MM-DD HH:mm"
              value={newQuiz.startAt}
              onChange={(e) => setNewQuiz({ ...newQuiz, startAt: e.target.value })}
            />
            <input
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="时间窗 结束 YYYY-MM-DD HH:mm"
              value={newQuiz.endAt}
              onChange={(e) => setNewQuiz({ ...newQuiz, endAt: e.target.value })}
            />
            <button
              className="px-4 py-2 gradient-bg text-white rounded-lg text-sm"
              onClick={handleCreateQuiz}
            >
              创建测验
            </button>
          </div>
          <div className="space-y-3 max-h-[500px] overflow-y-auto pr-1">
            {quizzes.length === 0 && <div className="text-slate-400 text-sm">暂无测验</div>}
            {quizzes.map((q) => {
              const subs = quizSubmissions[q.id] || [];
              return (
                <div key={q.id} className="p-3 rounded-lg border border-slate-100">
                  <div className="flex items-center justify-between">
                    <div className="text-sm font-semibold text-slate-800">{q.title}</div>
                    <div className="text-[11px] text-slate-500">
                      {q.startAt ? new Date(q.startAt).toLocaleString() : '未设置'} - {q.endAt ? new Date(q.endAt).toLocaleString() : '未设置'}
                    </div>
                  </div>
                  <div className="text-[11px] text-slate-500 mt-1">提交：{subs.length} 条</div>
                  <button
                    className="mt-2 text-[11px] text-indigo-500"
                    onClick={() => refreshQuizSubmissions(q.id)}
                  >
                    刷新提交
                  </button>
                  <button
                    className="ml-3 mt-2 text-[11px] text-rose-500"
                    onClick={() => handleDeleteQuiz(q.id)}
                  >
                    删除测验
                  </button>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {activeTab === 'enrollments' && (
        <section className="border border-slate-100 rounded-xl p-4 bg-white shadow-sm">
          <div className="text-sm font-bold text-slate-800 mb-2 flex items-center justify-between">
            <span>选课名单</span>
            {detailLoading && <span className="text-[11px] text-slate-400">加载中...</span>}
          </div>
          <div className="flex flex-col md:flex-row gap-2 mb-3">
            <select
              className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
              value={selectedStudentId}
              onChange={(e) => setSelectedStudentId(Number(e.target.value) || '')}
            >
              <option value="">选择学生</option>
              {students.map((s) => (
                <option key={s.id} value={s.id}>{s.username} ({s.className || '无班级'})</option>
              ))}
            </select>
            <button
              className="px-4 py-2 gradient-bg text-white rounded-lg text-sm"
              onClick={handleAssignEnrollment}
            >
              分配注册
            </button>
          </div>
          <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
            {enrollments.length === 0 && <div className="text-slate-400 text-sm">暂无选课</div>}
            {enrollments.map((en) => (
              <div key={en.id} className="p-3 rounded-lg border border-slate-100 flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-slate-800">{en.studentUsername || en.studentId}</div>
                  <div className="text-[11px] text-slate-500">
                    {(en.studentClassName || '')} {en.studentNumber ? `(${en.studentNumber})` : ''}
                  </div>
                </div>
                <div className="text-[11px] text-slate-400">
                  {en.createdAt ? new Date(en.createdAt).toLocaleString() : ''}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {activeTab === 'grades' && (
        <section className="border border-slate-100 rounded-xl p-4 bg-white shadow-sm">
          <div className="text-sm font-bold text-slate-800 mb-3 flex items-center justify-between">
            <span>成绩统计</span>
            {detailLoading && <span className="text-[11px] text-slate-400">加载中...</span>}
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-3 text-sm">
            <div className="p-3 rounded-lg bg-indigo-50 text-indigo-700">
              <div className="text-[11px]">平均分</div>
              <div className="text-lg font-bold">{gradeSummary?.average ?? '--'}</div>
            </div>
            <div className="p-3 rounded-lg bg-emerald-50 text-emerald-700">
              <div className="text-[11px]">最高分</div>
              <div className="text-lg font-bold">{gradeSummary?.max ?? '--'}</div>
            </div>
            <div className="p-3 rounded-lg bg-amber-50 text-amber-700">
              <div className="text-[11px]">最低分</div>
              <div className="text-lg font-bold">{gradeSummary?.min ?? '--'}</div>
            </div>
            <div className="p-3 rounded-lg bg-slate-50 text-slate-700">
              <div className="text-[11px]">学生数</div>
              <div className="text-lg font-bold">{studentGrades.length ?? 0}</div>
            </div>
          </div>
          <div className="max-h-[500px] overflow-y-auto pr-1 space-y-2">
            {studentGrades.length === 0 && <div className="text-slate-400 text-sm">暂无成绩</div>}
            {studentGrades.map((g) => (
              <div key={g.studentId} className="p-3 rounded-lg border border-slate-100 flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-slate-800">{g.studentUsername || g.studentId}</div>
                  <div className="text-[11px] text-slate-500">平均分：{g.averageScore ?? '--'}</div>
                </div>
                <div className="text-[11px] text-slate-500">
                  作业分：{g.assignmentScore ?? '--'} / 测验分：{g.quizScore ?? '--'}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
};

export default TeacherCenter;
