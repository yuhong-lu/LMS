
import React, { useEffect, useState } from 'react';
import { Course, DashboardSummary } from '../types';
import { apiService } from '../services/apiService';

const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        const [sumData, courseData] = await Promise.all([
          apiService.getSummary(),
          apiService.getCourses()
        ]);
        setSummary(sumData);
        setCourses(courseData);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };
    loadData();
  }, []);

  if (error) {
    return (
      <div className="p-10 text-center">
        <div className="bg-rose-50 text-rose-500 p-8 rounded-[2.5rem] border border-rose-100 inline-block shadow-sm">
          <i className="fa-solid fa-circle-exclamation mb-3 text-2xl"></i>
          <p className="font-bold text-sm">教务数据同步失败</p>
          <p className="text-[10px] mt-1 opacity-70">错误信息: {error}</p>
          <button onClick={() => window.location.reload()} className="mt-6 px-6 py-2 bg-white rounded-xl text-[10px] font-bold shadow-sm hover:bg-slate-50 transition-colors uppercase tracking-widest">刷新重试</button>
        </div>
      </div>
    );
  }

  const stats = [
    { label: '注册学员', value: summary?.totalStudents.toLocaleString() || '0', trend: '', icon: 'fa-user-graduate', color: 'text-indigo-500 bg-indigo-50/50' },
    { label: '在架课程', value: summary?.totalCourses.toString() || '0', trend: '', icon: 'fa-shapes', color: 'text-purple-500 bg-purple-50/50' },
    { label: '作业提交', value: summary?.assignmentSubmissions.toLocaleString() || '0', trend: '', icon: 'fa-file-lines', color: 'text-emerald-500 bg-emerald-50/50' },
    { label: '测验活跃', value: summary?.quizSubmissions.toLocaleString() || '0', trend: '', icon: 'fa-bolt', color: 'text-rose-500 bg-rose-50/50' },
  ];

  return (
    <div className="p-6 md:p-10 animate-in fade-in duration-1000">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-10">
        <div>
          <h1 className="text-2xl premium-title text-slate-800">概览仪表盘</h1>
          <p className="text-slate-400 text-xs mt-1.5 font-light tracking-wide">
            {isLoading ? '正在从云端读取最新教务摘要...' : '实时监测全站教学动态与选课流量。'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          { /* 这里保留创建入口给管理员/教师时可按需启用 */ }
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
        {stats.map((stat, i) => (
          <div key={i} className={`bg-white p-6 rounded-[1.8rem] border border-slate-50 soft-shadow group hover:border-indigo-100 transition-all ${isLoading ? 'animate-pulse' : ''}`}>
            <div className="flex items-center justify-between mb-4">
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${stat.color}`}>
                <i className={`fa-solid ${stat.icon} text-xs`}></i>
              </div>
              {!isLoading && <span className="text-[10px] font-bold px-2 py-0.5 bg-slate-50 rounded-full text-slate-400">{stat.trend}</span>}
            </div>
            <p className="text-[10px] font-bold text-slate-400 tracking-[0.15em] uppercase">{stat.label}</p>
            <h3 className="text-xl premium-title text-slate-800 mt-1">{isLoading ? '...' : stat.value}</h3>
          </div>
        ))}
      </div>

      <div className="flex items-center justify-between mb-6">
        <h2 className="text-[11px] font-bold text-slate-800 tracking-[0.25em] uppercase flex items-center gap-3">
          <div className="w-8 h-[1px] bg-indigo-100"></div>
          近期发布课程
        </h2>
        <button className="text-indigo-500 font-bold text-[10px] hover:underline tracking-[0.15em] uppercase">查看全部列表</button>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {isLoading ? (
          Array(4).fill(0).map((_, i) => (
            <div key={i} className="bg-slate-50/50 h-64 rounded-[2rem] animate-pulse border border-slate-100"></div>
          ))
        ) : (
          courses.slice(0, 4).map(course => (
            <div key={course.id} className="bg-white rounded-[2rem] border border-slate-50 overflow-hidden soft-shadow hover:-translate-y-1 transition-all duration-500 group">
              <div className="relative h-40 overflow-hidden bg-indigo-50">
                <img src={course.thumbnail || `https://picsum.photos/seed/${course.id}/600/400`} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-1000" />
              </div>
              <div className="p-6">
                <h3 className="premium-title text-slate-800 text-sm mb-1 line-clamp-1 group-hover:text-indigo-600 transition-colors leading-tight">{course.title}</h3>
                <p className="text-[10px] text-slate-400 mb-6 font-medium italic">{course.teacherName || '内部课程'}</p>
                <div className="flex items-center justify-between pt-5 border-t border-slate-50">
                   <span className="text-[9px] font-bold text-indigo-400 bg-indigo-50 px-2.5 py-1 rounded-lg uppercase tracking-tighter">
                      {course.category || '通识教育'}
                   </span>
                   <button className="w-8 h-8 rounded-full flex items-center justify-center text-slate-200 group-hover:bg-indigo-50 group-hover:text-indigo-500 transition-all">
                      <i className="fa-solid fa-arrow-right-long text-xs"></i>
                   </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Dashboard;
