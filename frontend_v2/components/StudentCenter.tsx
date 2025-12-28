import React, { useEffect, useState } from 'react';
import { apiService } from '../services/apiService';
import { Course } from '../types';

const StudentCenter: React.FC = () => {
  const [enrollments, setEnrollments] = useState<any[]>([]);
  const [courses, setCourses] = useState<Record<number, Course>>({});
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [resources, setResources] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileBase = (import.meta as any).env?.VITE_FILE_BASE || 'http://localhost:8080';

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

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const myEnroll = await apiService.listMyEnrollments();
        setEnrollments(myEnroll);
        if (myEnroll.length) {
          setSelectedCourseId(myEnroll[0].courseId);
        }
        // 预加载课程基础信息
        const allCourses = await apiService.getCourses();
        const map: Record<number, Course> = {};
        allCourses.forEach((c) => (map[c.id] = c));
        setCourses(map);
      } catch (err: any) {
        setError(err.message || '加载失败');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    const loadRes = async () => {
      if (!selectedCourseId) return;
      try {
        setLoading(true);
        const data = await apiService.getCourseResources(selectedCourseId);
        setResources(data);
      } catch (err: any) {
        setError(err.message || '资源加载失败');
      } finally {
        setLoading(false);
      }
    };
    loadRes();
  }, [selectedCourseId]);

  return (
    <div className="p-6 md:p-10">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-lg font-bold text-slate-800">学生中心</h1>
          <p className="text-slate-400 text-sm">查看我的课程与资源</p>
        </div>
        <select
          className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
          value={selectedCourseId || ''}
          onChange={(e) => setSelectedCourseId(Number(e.target.value))}
        >
          {enrollments.map((en) => (
            <option key={en.id} value={en.courseId}>
              {en.courseTitle}
            </option>
          ))}
        </select>
      </div>

      {error && (
        <div className="mb-4 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-4">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {enrollments.map((en) => (
          <div
            key={en.id}
            className={`p-4 rounded-xl border ${selectedCourseId === en.courseId ? 'border-indigo-200 bg-indigo-50/40' : 'border-slate-100'}`}
          >
            <div className="text-sm font-bold text-slate-800">{en.courseTitle}</div>
            <div className="text-xs text-slate-500">注册时间：{en.createdAt ? new Date(en.createdAt).toLocaleString() : ''}</div>
          </div>
        ))}
        {enrollments.length === 0 && !loading && <div className="text-slate-400 text-sm">暂无选课</div>}
      </div>

      <div className="mt-6">
        <h3 className="text-sm font-bold text-slate-700 mb-3">课程资源</h3>
        {loading ? (
          <div className="text-slate-400 text-sm">加载中...</div>
        ) : resources.length === 0 ? (
          <div className="text-slate-400 text-sm">暂无资源</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {resources.map((r) => (
              <div key={r.id} className="p-4 border border-slate-100 rounded-xl bg-white shadow-sm">
                <div className="text-sm font-bold text-slate-800">{r.title}</div>
                <div className="flex items-center justify-between mt-2 mb-2">
                  <span className="text-[11px] font-semibold text-indigo-500 bg-indigo-50 px-2 py-1 rounded-lg">
                    {typeLabel(r.type)}
                  </span>
                  {r.url && (
                    <div className="flex items-center gap-2">
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
                </div>
                {r.content && <p className="text-xs text-slate-600 mt-1 line-clamp-3">{r.content}</p>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentCenter;
