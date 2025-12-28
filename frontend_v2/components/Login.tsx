
import React, { useState } from 'react';
import { apiService } from '../services/apiService';
import Logo from './Logo';

interface LoginProps {
  onSuccess: (token: string) => void;
}

const Login: React.FC<LoginProps> = ({ onSuccess }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'ROLE_STUDENT' | 'ROLE_TEACHER'>('ROLE_STUDENT');
  const [studentNumber, setStudentNumber] = useState('');
  const [className, setClassName] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [isRegister, setIsRegister] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    try {
      const data = isRegister
        ? await apiService.register({ username, email, password, role, studentNumber, className })
        : await apiService.login({ username, password });
      localStorage.setItem('lms_token', data.token);
      onSuccess(data.token);
    } catch (err: any) {
      setError(err.message || (isRegister ? '注册失败，请检查填写信息' : '登录失败，请检查账号密码'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#fcfcfd] p-6">
      <div className="w-full max-w-md bg-white rounded-[3rem] soft-shadow border border-slate-50 p-10 md:p-12 animate-in zoom-in duration-500">
        <div className="flex flex-col items-center mb-10">
          <Logo className="scale-125 mb-6" />
          <h2 className="text-xl premium-title text-slate-800">
            {isRegister ? '注册新账号' : '登入您的 EduFlow'}
          </h2>
          <p className="text-slate-400 text-[11px] mt-2 font-medium tracking-widest uppercase">智能教务管理协作系统</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-1.5">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">用户名</label>
            <div className="relative group">
              <i className="fa-solid fa-user absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors"></i>
              <input 
                type="text" 
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="w-full pl-12 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                placeholder="请输入用户名..."
              />
            </div>
          </div>

          {isRegister && (
            <>
              <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">邮箱</label>
                <div className="relative group">
                  <i className="fa-solid fa-envelope absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors"></i>
                  <input 
                    type="email" 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full pl-12 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                    placeholder="请输入邮箱..."
                  />
                </div>
              </div>
              <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">角色</label>
                <div className="relative group">
                  <select
                    className="w-full pl-4 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                    value={role}
                    onChange={(e) => setRole(e.target.value as any)}
                  >
                    <option value="ROLE_STUDENT">学生</option>
                    <option value="ROLE_TEACHER">教师</option>
                  </select>
                </div>
              </div>
              <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">学号 / 工号（可选）</label>
                <div className="relative group">
                  <i className="fa-solid fa-id-card absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors"></i>
                  <input 
                    type="text" 
                    value={studentNumber}
                    onChange={(e) => setStudentNumber(e.target.value)}
                    className="w-full pl-12 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                    placeholder="方便匹配学籍，可不填"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">班级（学生建议填写）</label>
                <div className="relative group">
                  <i className="fa-solid fa-users absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors"></i>
                  <input 
                    type="text" 
                    value={className}
                    onChange={(e) => setClassName(e.target.value)}
                    className="w-full pl-12 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                    placeholder="如：计科2025-1班，可不填"
                  />
                </div>
              </div>
            </>
          )}

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-4">密码</label>
            <div className="relative group">
              <i className="fa-solid fa-lock absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors"></i>
              <input 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full pl-12 pr-6 py-4 bg-slate-50/50 border border-transparent rounded-2xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                placeholder="请输入密码..."
              />
            </div>
          </div>

          {error && (
            <div className="p-4 bg-rose-50 border border-rose-100 rounded-xl text-rose-500 text-[10px] font-bold flex items-center gap-2">
              <i className="fa-solid fa-circle-exclamation"></i>
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-4 gradient-bg text-white rounded-2xl font-bold text-xs tracking-widest uppercase shadow-xl shadow-indigo-500/20 hover:scale-[1.01] active:scale-95 disabled:opacity-50 transition-all flex items-center justify-center gap-2"
          >
            {isLoading ? <i className="fa-solid fa-spinner animate-spin"></i> : isRegister ? '立即注册' : '立即登入'}
          </button>
        </form>

        <p className="mt-8 text-center text-slate-300 text-[10px] font-medium">
          {isRegister ? '已有账号？' : '还没有账号？'}{' '}
          <span
            className="text-indigo-400 cursor-pointer hover:underline font-bold"
            onClick={() => {
              setIsRegister((v) => !v);
              setError('');
            }}
          >
            {isRegister ? '去登录' : '申请注册'}
          </span>
        </p>
      </div>
    </div>
  );
};

export default Login;
