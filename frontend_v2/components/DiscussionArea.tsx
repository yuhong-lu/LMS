
import React, { useEffect, useState } from 'react';
import { DiscussionTopic, Course } from '../types';
import { apiService } from '../services/apiService';

const DiscussionArea: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [topics, setTopics] = useState<DiscussionTopic[]>([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const init = async () => {
      try {
        const cs = await apiService.getCourses();
        setCourses(cs);
        if (cs.length) {
          setSelectedCourseId(cs[0].id);
        }
      } catch (err: any) {
        setError(err.message || '课程加载失败');
      }
    };
    init();
  }, []);

  useEffect(() => {
    const loadTopics = async () => {
      if (!selectedCourseId) return;
      try {
        setLoading(true);
        setError(null);
        const data = await apiService.getTopics(String(selectedCourseId));
        setTopics(data);
      } catch (err: any) {
        setError(err.message || '讨论加载失败');
      } finally {
        setLoading(false);
      }
    };
    loadTopics();
  }, [selectedCourseId]);

  const createTopic = async () => {
    if (!selectedCourseId || !title.trim() || !content.trim()) return;
    try {
      setLoading(true);
      await apiService.createTopic(selectedCourseId, { title, content });
      setTitle('');
      setContent('');
      const data = await apiService.getTopics(String(selectedCourseId));
      setTopics(data);
    } catch (err: any) {
      setError(err.message || '创建话题失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 md:p-10 animate-in fade-in slide-in-from-right-2 duration-700">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-lg font-bold text-slate-800 tracking-tight">学术讨论区</h1>
          <p className="text-slate-400 text-[11px] mt-1 font-medium">师生互动中心，共享知识与见解。</p>
        </div>
        <div className="flex gap-2">
          <select
            className="px-4 py-2 rounded-xl border border-slate-200 text-[12px] text-slate-600"
            value={selectedCourseId || ''}
            onChange={(e) => setSelectedCourseId(Number(e.target.value))}
          >
            {courses.map((c) => (
              <option key={c.id} value={c.id}>{c.title}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="bg-white border border-slate-100 rounded-2xl p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <input
            className="px-3 py-2 border border-slate-200 rounded-lg text-sm"
            placeholder="话题标题"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <div className="flex gap-2">
            <textarea
              className="flex-1 px-3 py-2 border border-slate-200 rounded-lg text-sm"
              placeholder="说点什么..."
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
            <button
              onClick={createTopic}
              disabled={loading || !selectedCourseId || !title.trim() || !content.trim()}
              className="px-4 py-2 gradient-bg text-white rounded-lg text-[12px] font-bold disabled:opacity-50"
            >
              发布
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="mb-4 text-rose-500 text-sm bg-rose-50 border border-rose-100 rounded-xl p-4">
          {error}
        </div>
      )}

      <div className="space-y-4">
        {(loading ? Array(3).fill(null) : topics).map((thread, idx) => (
          <div key={thread?.id || idx} className="bg-white p-6 rounded-[2rem] border border-slate-50 soft-shadow hover:border-indigo-100 transition-all group">
            <div className="flex gap-4">
              <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${thread?.authorUsername || 'user'}`} className="w-10 h-10 rounded-full bg-slate-50" alt="" />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs font-bold text-slate-800 group-hover:text-indigo-600 transition-colors">
                    {thread ? thread.authorUsername : '加载中...'}
                  </span>
                  <span className="text-[10px] text-slate-300 font-medium italic">
                    • {thread?.createdAt ? new Date(thread.createdAt).toLocaleString() : '...'}
                  </span>
                </div>
                <p className="text-xs text-slate-600 leading-relaxed mb-2 font-bold">{thread?.title || ''}</p>
                <p className="text-xs text-slate-600 leading-relaxed mb-4">{thread?.content || ''}</p>
                <div className="flex items-center justify-between">
                    <span className="text-[9px] font-bold text-indigo-400 bg-indigo-50 px-2 py-0.5 rounded-full uppercase tracking-tighter">
                        课程：{courses.find((c) => c.id === thread?.courseId)?.title || 'N/A'}
                    </span>
                    <div className="flex items-center gap-4 text-slate-300">
                        <span className="flex items-center gap-1.5 text-[10px] font-bold">
                            <i className="fa-regular fa-comment-dots"></i>
                        </span>
                    </div>
                </div>
              </div>
            </div>
          </div>
        ))}
        {!loading && topics.length === 0 && (
          <div className="text-center text-slate-400 text-sm py-6">暂无话题</div>
        )}
      </div>
    </div>
  );
};

export default DiscussionArea;
