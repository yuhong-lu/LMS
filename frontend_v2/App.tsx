
import React, { useState, useEffect } from 'react';
import Logo from './components/Logo';
import Dashboard from './components/Dashboard';
import CourseManagement from './components/CourseManagement';
import StudentCenter from './components/StudentCenter';
import TeacherCenter from './components/TeacherCenter';
import DiscussionArea from './components/DiscussionArea';
import ProfileCenter from './components/ProfileCenter';
import Login from './components/Login';
import { AppView, User } from './types';
import { apiService } from './services/apiService';

const App: React.FC = () => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('lms_token'));
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [currentView, setCurrentView] = useState<AppView>('dashboard');

  useEffect(() => {
    if (token) {
      apiService.getMe().then(setCurrentUser).catch(() => setToken(null));
    }
  }, [token]);

  if (!token) {
    return <Login onSuccess={(t) => setToken(t)} />;
  }

  const handleLogout = () => {
    localStorage.removeItem('lms_token');
    setToken(null);
    setCurrentUser(null);
  };

  const isAdmin = currentUser?.role === 'ROLE_ADMIN';
  const isTeacher = currentUser?.role === 'ROLE_TEACHER';
  const isStudent = currentUser?.role === 'ROLE_STUDENT';

  return (
    <div className="min-h-screen bg-[#fcfcfd] flex">
      {/* 侧边栏 */}
      <aside className="hidden lg:flex w-64 bg-[#f4f6ff] border-r border-indigo-100/60 flex-col sticky top-0 h-screen select-none">
        <div className="p-8 pb-10">
          <Logo />
        </div>
        
        <nav className="flex-1 px-4 space-y-1.5 overflow-y-auto hide-scrollbar">
          <NavItem isActive={currentView === 'dashboard'} onClick={() => setCurrentView('dashboard')} icon="fa-columns" label="仪表盘" />
          <NavItem isActive={currentView === 'courses'} onClick={() => setCurrentView('courses')} icon="fa-book-bookmark" label="课程库" />
          {(isTeacher || isAdmin) && (
            <NavItem isActive={currentView === 'teachers'} onClick={() => setCurrentView('teachers')} icon="fa-user-tie" label="教师中心" />
          )}
          {isStudent && (
            <NavItem isActive={currentView === 'students'} onClick={() => setCurrentView('students')} icon="fa-graduation-cap" label="学生中心" />
          )}
          <NavItem isActive={currentView === 'discussions'} onClick={() => setCurrentView('discussions')} icon="fa-comments" label="讨论区" />
          <div className="px-4 py-4 text-[13px] font-semibold leading-snug text-indigo-500 whitespace-nowrap" style={{ fontFamily: '"Noto Serif SC","SimSun","宋体",serif' }}>
            <div>学习如水</div>
            <div>在 EduFlow 中自由流动</div>
          </div>
        </nav>

        <div className="p-6">
          <button 
            onClick={() => setCurrentView('profile')}
            className={`w-full group rounded-2xl p-3.5 flex items-center gap-3 border transition-all duration-500 ${
              currentView === 'profile' 
                ? 'bg-white border-indigo-200 shadow-lg shadow-indigo-500/5' 
                : 'bg-indigo-50/50 border-transparent hover:bg-white hover:border-indigo-100 hover:shadow-sm'
            }`}
          >
            <div className="w-10 h-10 rounded-xl gradient-bg p-0.5 flex items-center justify-center shadow-sm relative overflow-hidden">
               <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser?.username || 'Felix'}`} className="w-full h-full object-cover rounded-[10px]" alt="Avatar" />
               <div className="absolute bottom-0.5 right-0.5 w-2 h-2 bg-emerald-400 border-2 border-white rounded-full"></div>
            </div>
            <div className="flex-1 min-w-0 text-left">
              <p className="text-[11px] font-bold text-slate-700 truncate group-hover:text-indigo-600 transition-colors">
                {currentUser?.username || '加载中...'}
              </p>
              <p className="text-[9px] text-slate-400 font-bold uppercase tracking-tighter">
                {currentUser
                  ? currentUser.role === 'ROLE_ADMIN'
                    ? '超级管理员'
                    : currentUser.role === 'ROLE_TEACHER'
                      ? '授课教师'
                      : '在校学生'
                  : '加载中'}
              </p>
            </div>
          </button>
        </div>
      </aside>

      {/* 主内容区域 */}
      <main className="flex-1 flex flex-col h-screen overflow-y-auto hide-scrollbar bg-white">
        <header className="lg:hidden bg-white/80 border-b border-indigo-50 p-4 sticky top-0 z-30 flex items-center justify-between backdrop-blur-xl">
          <Logo className="scale-90 origin-left" />
          <div className="flex items-center gap-3">
            <button onClick={() => setCurrentView('profile')} className="w-9 h-9 rounded-xl gradient-bg p-0.5 overflow-hidden border-2 border-white shadow-sm">
               <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser?.username}`} className="w-full h-full object-cover rounded-[8px]" alt="Avatar" />
            </button>
          </div>
        </header>

        <div className="flex-1 relative">
          {currentView === 'dashboard' && <Dashboard />}
          {currentView === 'courses' && <CourseManagement currentUser={currentUser} />}
          {currentView === 'teachers' && <TeacherCenter currentUser={currentUser} />}
          {currentView === 'students' && <StudentCenter />}
          {currentView === 'discussions' && <DiscussionArea />}
          {currentView === 'profile' && <ProfileCenter user={currentUser} onLogout={handleLogout} />}
        </div>
      </main>
    </div>
  );
};

interface NavItemProps {
  icon: string;
  label: string;
  isActive?: boolean;
  onClick?: () => void;
}

const NavItem: React.FC<NavItemProps> = ({ icon, label, isActive, onClick }) => (
  <button 
    onClick={onClick}
    className={`w-full flex items-center gap-4 px-5 py-3.5 rounded-2xl font-bold transition-all duration-500 group relative overflow-hidden ${
      isActive ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
    }`}
  >
    <div className={`absolute inset-0 transition-all duration-500 ${isActive ? 'opacity-100 scale-100' : 'opacity-0 scale-95'}`}>
        <div className="absolute inset-0 bg-white shadow-[0_2px_12px_-4px_rgba(79,70,229,0.12)] border border-indigo-100/50 rounded-2xl"></div>
        <div className="absolute left-0 top-3.5 bottom-3.5 w-1 gradient-bg rounded-r-full"></div>
    </div>
    <div className={`relative z-10 w-5 flex justify-center transition-transform duration-500 ${isActive ? 'scale-110' : 'group-hover:translate-x-0.5'}`}>
        <i className={`fa-solid ${icon} text-[14px] transition-colors duration-500 ${isActive ? 'gradient-text' : 'text-slate-300 group-hover:text-slate-400'}`}></i>
    </div>
    <span className="text-[12px] tracking-tight relative z-10 transition-all duration-500 font-semibold">{label}</span>
    {isActive && <div className="absolute right-5 w-1 h-1 rounded-full gradient-bg animate-pulse"></div>}
  </button>
);

export default App;
