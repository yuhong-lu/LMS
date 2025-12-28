import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Course, User } from '../types';
import { apiService } from '../services/apiService';

interface Props {
  currentUser: User | null;
}

const CourseManagement: React.FC<Props> = ({ currentUser }) => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null);
  const [enrollments, setEnrollments] = useState<any[]>([]);
  const [enrollmentsLoading, setEnrollmentsLoading] = useState(false);
  const [detailCourse, setDetailCourse] = useState<Course | null>(null);
  const [resourceCourse, setResourceCourse] = useState<Course | null>(null);
  const [resourceList, setResourceList] = useState<any[]>([]);
  const [resourceLoading, setResourceLoading] = useState(false);
  const [resourceError, setResourceError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const isAdmin = currentUser?.role === 'ROLE_ADMIN';
  const isTeacher = currentUser?.role === 'ROLE_TEACHER';
  const canManageEnroll = isAdmin || isTeacher;
  const [showCreate, setShowCreate] = useState(false);
  const [newCourse, setNewCourse] = useState({
    title: '',
    description: '',
    syllabus: '',
    teacherId: 0,
  });
  const [newResource, setNewResource] = useState({ title: '', type: 'TEXT', url: '', content: '' });
  const [uploading, setUploading] = useState(false);
  const fileBase = (import.meta as any).env?.VITE_FILE_BASE || 'http://localhost:8080';

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await apiService.getCourses();
        setCourses(data);
        const allUsers = isAdmin ? await apiService.listUsers() : await apiService.listStudents();
        setUsers(allUsers);
      } catch (err: any) {
        setError(err.message || '课程加载失败');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [isAdmin]);

  const students = useMemo(
    () => users.filter((u) => u.role === 'ROLE_STUDENT'),
    [users],
  );

  const resolveUrl = (url?: string) => {
    if (!url) return '';
    return url.startsWith('http') ? url : `${fileBase}${url}`;
  };

  const typeLabel = (type?: string) => {
    switch ((type || '').toUpperCase()) {
      case 'TEXT':
        return '文本';
      case 'AUDIO':
        return '音频';
      case 'VIDEO':
        return '视频';
      case 'FILE':
        return '文件';
      case 'LINK':
        return '链接';
      default:
        return type || '未知';
    }
  };

  const loadEnrollments = async (courseId: number) => {
    if (!canManageEnroll) return;
    try {
      setEnrollmentsLoading(true);
      const data = await apiService.listEnrollments(courseId);
      setEnrollments(data);
    } catch (err: any) {
      setError(err.message || '注册列表加载失败');
    } finally {
      setEnrollmentsLoading(false);
    }
  };

  const openDetailModal = (course: Course) => {
    setDetailCourse(course);
    setSelectedStudentId(null);
    if (canManageEnroll) {
      loadEnrollments(course.id);
    }
  };

  const closeDetailModal = () => {
    setDetailCourse(null);
    setEnrollments([]);
    setSelectedStudentId(null);
  };

  const openResourceModal = async (course: Course) => {
    setResourceCourse(course);
    setResourceError(null);
    setResourceLoading(true);
    setNewResource({ title: '', type: 'TEXT', url: '', content: '' });
    try {
      const data = await apiService.getCourseResources(course.id);
      setResourceList(data);
    } catch (err: any) {
      setResourceError(err.message || '资源加载失败');
    } finally {
      setResourceLoading(false);
    }
  };

  const closeResourceModal = () => {
    setResourceCourse(null);
    setResourceList([]);
    setResourceError(null);
    setNewResource({ title: '', type: 'TEXT', url: '', content: '' });
  };

  const handleAssign = async () => {
    if (!detailCourse?.id || !selectedStudentId) return;
    try {
      await apiService.assignEnrollment({ courseId: detailCourse.id, studentId: selectedStudentId });
      await loadEnrollments(detailCourse.id);
    } catch (err: any) {
      setError(err.message || '分配失败');
    }
  };

  const handleRemove = async (id: number) => {
    if (!detailCourse?.id) return;
    await apiService.removeEnrollment(id);
    await loadEnrollments(detailCourse.id);
  };

  const handleCreate = async () => {
    if (!newCourse.title || !newCourse.teacherId) {
      setError('请填写课程名称并选择授课教师');
      return;
    }
    try {
      setError(null);
      const created = await apiService.createCourse({
        title: newCourse.title,
        description: newCourse.description,
        syllabus: newCourse.syllabus,
        teacherId: newCourse.teacherId,
      });
      setCourses((prev) => [created, ...prev]);
      setShowCreate(false);
      setNewCourse({ title: '', description: '', syllabus: '', teacherId: 0 });
    } catch (err: any) {
      setError(err.message || '创建失败');
    }
  };

  const handleAddResource = async () => {
    if (!resourceCourse?.id || !newResource.title) {
      setResourceError('请先选择课程并填写资源标题');
      return;
    }
    try {
      const created = await apiService.addCourseResource(resourceCourse.id, newResource);
      setResourceList((prev) => [created, ...prev]);
      setNewResource({ title: '', type: 'TEXT', url: '', content: '' });
    } catch (err: any) {
      setResourceError(err.message || '新增资源失败');
    }
  };

  const handleFileSelect = async (files?: FileList | null) => {
    if (!files || files.length === 0 || !resourceCourse?.id) return;
    const file = files[0];
    try {
      setUploading(true);
      const result = await apiService.uploadFile(file, resourceCourse.id);
      const guess = (() => {
        const mime = file.type?.toLowerCase() || '';
        const ext = file.name.split('.').pop()?.toLowerCase() || '';
        if (mime.startsWith('video/') || ['mp4', 'mov', 'avi', 'mkv'].includes(ext)) return 'VIDEO';
        if (mime.startsWith('audio/') || ['mp3', 'wav', 'aac'].includes(ext)) return 'AUDIO';
        return 'FILE';
      })();
      setNewResource((prev) => ({
        ...prev,
        url: result.url,
        title: prev.title || file.name,
        type: guess,
      }));
    } catch (err: any) {
      setError(err.message || '上传失败');
    } finally {
      setUploading(false);
    }
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    handleFileSelect(e.dataTransfer.files);
  };

  const handleDeleteResource = async (id: number) => {
    if (!id || !resourceCourse?.id) return;
    try {
      await apiService.deleteCourseResource(id);
      setResourceList((prev) => prev.filter((r) => r.id !== id));
    } catch (err: any) {
      setResourceError(err.message || '删除资源失败');
    }
  };

  return (
    <div className="p-6 md:p-10 animate-in fade-in slide-in-from-right-2 duration-700">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-xl premium-title text-slate-800">课程管理库</h1>
          <p className="text-slate-400 text-[11px] mt-1.5 font-medium tracking-wide">以卡片浏览每门课程，快捷查看详情与资料。</p>
        </div>
        <div className="flex gap-2">
          <button className="px-5 py-2.5 bg-white border border-slate-100 soft-shadow rounded-xl text-[11px] font-bold text-slate-600 hover:bg-slate-50 transition-all">
            导出报表
          </button>
          {isAdmin && (
            <button
              className="px-6 py-2.5 gradient-bg text-white rounded-xl text-[11px] font-bold hover:shadow-xl hover:shadow-indigo-500/20 transition-all flex items-center gap-2"
              onClick={() => setShowCreate(true)}
            >
              <i className="fa-solid fa-plus-circle"></i>
              新建课程
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-4 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-4">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        {(loading ? Array(6).fill(null) : courses).map((course, idx) => (
          <div
            key={course?.id || idx}
            className="bg-white rounded-2xl border border-slate-100 soft-shadow hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300 flex flex-col h-full"
          >
            <div className="relative h-40 w-full rounded-t-2xl overflow-hidden">
              {course ? (
                <img
                  src={course.thumbnail || `https://picsum.photos/seed/${course.id}/600/400`}
                  alt={course.title}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full animate-pulse bg-slate-100" />
              )}
              <span className="absolute top-3 right-3 text-[11px] font-bold text-emerald-600 bg-emerald-50 border border-emerald-100 px-2 py-1 rounded-full">
                已发布
              </span>
            </div>
            <div className="p-4 flex-1 flex flex-col gap-2">
              <div>
                <p className="text-sm font-bold text-slate-800 line-clamp-1">{course ? course.title : '加载中...'}</p>
                <p className="text-xs text-slate-500 line-clamp-2 mt-1">
                  {course?.description || '课程简介将在此展示，便于快速了解课程亮点。'}
                </p>
              </div>
              <div className="flex items-center justify-between text-[11px] text-slate-400">
                <span>{course?.teacherName || (course ? `教师 #${course.teacherId || '-'}` : '教师')}</span>
                <span>{course?.createdAt ? new Date(course.createdAt).toLocaleDateString() : ''}</span>
              </div>
            </div>
            <div className="px-4 pb-4 flex items-center justify-end gap-2">
              <button
                onClick={() => course && openDetailModal(course)}
                className="px-3 py-1.5 rounded-lg text-xs font-semibold border border-indigo-100 bg-indigo-500/10 text-indigo-600 hover:bg-indigo-500/20 hover:border-indigo-200 transition-all backdrop-blur"
                disabled={!course}
              >
                课程详情
              </button>
            </div>
          </div>
        ))}
        {!loading && courses.length === 0 && (
          <div className="text-slate-400 text-sm col-span-full">暂无课程</div>
        )}
      </div>

      {/* 创建课程 */}
      {showCreate && isAdmin && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-[480px] shadow-xl border border-slate-100">
            <h3 className="text-lg font-bold mb-4">创建新课程</h3>
            <div className="space-y-3">
              <input
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                placeholder="课程名称"
                value={newCourse.title}
                onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })}
              />
              <textarea
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                placeholder="课程简介"
                value={newCourse.description}
                onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })}
              />
              <textarea
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                placeholder="课程大纲/备注"
                value={newCourse.syllabus}
                onChange={(e) => setNewCourse({ ...newCourse, syllabus: e.target.value })}
              />
              <select
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                value={newCourse.teacherId || ''}
                onChange={(e) => setNewCourse({ ...newCourse, teacherId: Number(e.target.value) })}
              >
                <option value="">选择授课教师</option>
                {users.filter((u) => u.role === 'ROLE_TEACHER' || u.role === 'ROLE_ADMIN').map((t) => (
                  <option key={t.id} value={t.id}>{t.username}</option>
                ))}
              </select>
            </div>
            <div className="flex justify-end gap-2 mt-5">
              <button className="px-4 py-2 text-slate-500" onClick={() => setShowCreate(false)}>取消</button>
              <button className="px-4 py-2 gradient-bg text-white rounded-lg text-sm" onClick={handleCreate}>
                保存
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 课程详情弹窗 */}
      {detailCourse && (
        <div className="fixed inset-0 bg-black/40 flex items-start justify-center z-50 overflow-y-auto py-10 px-4">
          <div className="bg-white w-full max-w-4xl rounded-2xl shadow-2xl border border-slate-100 p-6 relative">
            <button className="absolute right-4 top-4 text-slate-400 hover:text-slate-600" onClick={closeDetailModal}>
              <i className="fa-solid fa-xmark"></i>
            </button>
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <h3 className="text-xl font-bold text-slate-800 mb-2">{detailCourse.title}</h3>
                <div className="flex flex-wrap gap-3 text-sm text-slate-500">
                  <span>讲师：{detailCourse.teacherName || `#${detailCourse.teacherId || '-'}`}</span>
                  <span>创建：{detailCourse.createdAt ? new Date(detailCourse.createdAt).toLocaleDateString() : '—'}</span>
                </div>
                <p className="text-sm text-slate-700 leading-relaxed mt-3 whitespace-pre-line">
                  {detailCourse.description || '暂无简介'}
                </p>
                {detailCourse.syllabus && (
                  <div className="mt-4 p-3 rounded-xl bg-indigo-50/60 border border-indigo-100 text-sm text-slate-700 whitespace-pre-line">
                    <div className="text-[11px] font-semibold text-indigo-500 mb-1">课程大纲</div>
                    {detailCourse.syllabus}
                  </div>
                )}
              </div>
              <div className="w-full md:w-80 bg-slate-50/80 border border-slate-100 rounded-2xl p-4">
                <div className="text-xs text-slate-400 mb-2">快速操作</div>
                <div className="space-y-2">
                  <button
                    className="w-full px-4 py-2 rounded-lg border border-indigo-100 text-indigo-600 bg-white hover:bg-indigo-50 transition-all text-sm font-semibold"
                    onClick={() => openResourceModal(detailCourse)}
                  >
                    编辑资料
                  </button>
                  {canManageEnroll && (
                    <div className="space-y-2">
                      <div className="text-xs text-slate-500">分配学生</div>
                      <select
                        className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                        value={selectedStudentId || ''}
                        onChange={(e) => setSelectedStudentId(Number(e.target.value))}
                      >
                        <option value="">选择学生</option>
                        {students.map((s) => (
                          <option key={s.id} value={s.id}>{s.username} ({s.className || '无班级'})</option>
                        ))}
                      </select>
                      <button
                        onClick={handleAssign}
                        disabled={!selectedStudentId}
                        className="w-full px-4 py-2 gradient-bg text-white rounded-lg text-sm disabled:opacity-60"
                      >
                        分配注册
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {canManageEnroll && (
              <div className="mt-6">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="text-sm font-bold text-slate-800">注册名单</h4>
                  {enrollmentsLoading && <span className="text-xs text-slate-400">加载中...</span>}
                </div>
                <div className="overflow-x-auto rounded-xl border border-slate-100">
                  <table className="w-full text-left text-sm">
                    <thead>
                      <tr className="bg-slate-50 border-b border-slate-100">
                        <th className="px-4 py-2">学生</th>
                        <th className="px-4 py-2">班级/学号</th>
                        <th className="px-4 py-2">注册时间</th>
                        <th className="px-4 py-2 text-right">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      {enrollments.map((en) => (
                        <tr key={en.id} className="border-b border-slate-50">
                          <td className="px-4 py-2">{en.studentUsername || en.studentId}</td>
                          <td className="px-4 py-2 text-slate-500 text-sm">
                            {(en.studentClassName || '')} {en.studentNumber ? `(${en.studentNumber})` : ''}
                          </td>
                          <td className="px-4 py-2 text-slate-500 text-sm">
                            {en.createdAt ? new Date(en.createdAt).toLocaleString() : ''}
                          </td>
                          <td className="px-4 py-2 text-right">
                            <button
                              className="text-rose-500 text-sm"
                              onClick={() => handleRemove(en.id)}
                            >
                              移除
                            </button>
                          </td>
                        </tr>
                      ))}
                      {enrollments.length === 0 && (
                        <tr>
                          <td colSpan={4} className="px-4 py-4 text-slate-400 text-sm text-center">暂无注册</td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 资料弹窗 */}
      {resourceCourse && (
        <div className="fixed inset-0 bg-black/40 flex items-start justify-center z-50 overflow-y-auto py-10 px-4">
          <div className="bg-white w-full max-w-5xl rounded-2xl shadow-2xl border border-slate-100 p-6 relative">
            <button className="absolute right-4 top-4 text-slate-400 hover:text-slate-600" onClick={closeResourceModal}>
              <i className="fa-solid fa-xmark"></i>
            </button>
            <div className="flex items-center justify-between mb-4">
              <div>
                <div className="text-[11px] text-slate-400">课程资料</div>
                <h3 className="text-lg font-bold text-slate-800">{resourceCourse.title}</h3>
              </div>
              <span className="text-[11px] text-slate-400">
                {resourceLoading ? '加载中...' : `${resourceList.length} 项资料`}
              </span>
            </div>

            {resourceError && (
              <div className="mb-3 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-3">
                {resourceError}
              </div>
            )}

            {canManageEnroll && (
              <div className="mb-4 flex flex-col lg:flex-row lg:items-center gap-2">
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
                  新增
                </button>
              </div>
            )}

            {canManageEnroll && (
              <div
                className="mt-3 border-2 border-dashed border-indigo-200 rounded-2xl bg-indigo-50/60 text-indigo-500 text-sm flex flex-col items-center justify-center py-10 px-4 cursor-pointer"
                onDragOver={(e) => e.preventDefault()}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <i className="fa-solid fa-cloud-arrow-up text-xl mb-2"></i>
                <div className="font-semibold">{uploading ? '上传中...' : '拖拽文件到此或点击选择'}</div>
                <div className="text-[11px] text-indigo-400 mt-1">支持课程资料、音视频、课件等</div>
                <input
                  type="file"
                  ref={fileInputRef}
                  className="hidden"
                  onChange={(e) => handleFileSelect(e.target.files)}
                />
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {resourceLoading ? (
                Array(4).fill(null).map((_, i) => (
                  <div key={i} className="h-24 rounded-xl bg-slate-100 animate-pulse" />
                ))
              ) : resourceList.length === 0 ? (
                <div className="text-slate-400 text-sm">暂无资源</div>
              ) : (
                resourceList.map((r) => (
                  <div key={r.id} className="p-4 border border-slate-100 rounded-xl bg-white shadow-sm">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-bold text-slate-800 line-clamp-1">{r.title}</div>
                      <span className="text-[11px] font-semibold text-indigo-500 bg-indigo-50 px-2 py-1 rounded-lg">
                        {typeLabel(r.type)}
                      </span>
                    </div>
                    {r.content && <p className="text-xs text-slate-600 mt-1 line-clamp-2">{r.content}</p>}
                    {r.url && (
                      <div className="flex items-center gap-3 mt-2">
                        <a
                          className="text-indigo-500 text-xs"
                          href={resolveUrl(r.url)}
                          target="_blank"
                          rel="noreferrer"
                          download
                        >
                          下载
                        </a>
                        <button
                          className="text-xs text-slate-500 underline"
                          onClick={() => window.open(resolveUrl(r.url), '_blank')}
                        >
                          预览
                        </button>
                      </div>
                    )}
                    {canManageEnroll && (
                      <div className="flex justify-end mt-2">
                        <button
                          className="text-rose-500 text-xs"
                          onClick={() => handleDeleteResource(r.id)}
                        >
                          删除
                        </button>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CourseManagement;
