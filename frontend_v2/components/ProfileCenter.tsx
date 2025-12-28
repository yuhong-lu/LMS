
import React from 'react';
import { User } from '../types';

interface ProfileCenterProps {
  user: User | null;
  onLogout?: () => void;
}

const ProfileCenter: React.FC<ProfileCenterProps> = ({ user, onLogout }) => {
  if (!user) return null;

  return (
    <div className="p-6 md:p-10 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-[3rem] border border-slate-50 soft-shadow overflow-hidden">
          <div className="h-40 gradient-bg relative overflow-hidden">
             <div className="absolute inset-0 bg-white/10 backdrop-blur-3xl"></div>
             <div className="absolute -bottom-16 left-10 p-1 bg-white rounded-[2.5rem] shadow-2xl">
                <div className="w-32 h-32 rounded-[2.2rem] gradient-bg overflow-hidden border-4 border-white">
                   <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`} className="w-full h-full object-cover" alt="Profile" />
                </div>
             </div>
          </div>
          <div className="pt-20 pb-10 px-10">
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
              <div>
                <div className="flex items-center gap-3">
                  <h1 className="text-2xl premium-title text-slate-800">{user.username}</h1>
                  <span className="px-2.5 py-0.5 bg-indigo-50 text-indigo-600 rounded-full text-[9px] font-bold uppercase tracking-wider border border-indigo-100">
                    {user.role.replace('ROLE_', '')}
                  </span>
                </div>
                <p className="text-xs text-slate-400 font-medium mt-1.5 flex items-center gap-2">
                  <i className="fa-solid fa-envelope text-slate-300"></i> {user.email}
                </p>
              </div>
              <div className="flex gap-2">
                <button className="px-6 py-2.5 bg-slate-50 text-slate-600 rounded-xl text-[10px] font-bold border border-slate-100 hover:bg-slate-100 transition-all tracking-widest uppercase">设置资料</button>
                <button
                  className="px-6 py-2.5 gradient-bg text-white rounded-xl text-[10px] font-bold shadow-lg shadow-indigo-500/20 tracking-widest uppercase"
                  onClick={onLogout}
                >
                  退出登录
                </button>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12">
              <div className="bg-slate-50/50 p-6 rounded-[2rem] border border-slate-100/50 group hover:bg-white hover:soft-shadow transition-all">
                 <p className="text-[9px] font-bold text-slate-300 uppercase tracking-[0.2em] mb-2">学工号 / 编号</p>
                 <p className="text-sm font-bold text-slate-700 flex items-center gap-2">
                   <i className="fa-solid fa-id-card-clip text-indigo-300"></i> {user.studentNumber || '未绑定'}
                 </p>
              </div>
              <div className="bg-slate-50/50 p-6 rounded-[2rem] border border-slate-100/50 group hover:bg-white hover:soft-shadow transition-all">
                 <p className="text-[9px] font-bold text-slate-300 uppercase tracking-[0.2em] mb-2">所属班级</p>
                 <p className="text-sm font-bold text-slate-700 flex items-center gap-2">
                   <i className="fa-solid fa-users-rectangle text-purple-300"></i> {user.className || '教职员工组'}
                 </p>
              </div>
              <div className="bg-slate-50/50 p-6 rounded-[2rem] border border-slate-100/50 group hover:bg-white hover:soft-shadow transition-all">
                 <p className="text-[9px] font-bold text-slate-300 uppercase tracking-[0.2em] mb-2">注册于</p>
                 <p className="text-sm font-bold text-slate-700 flex items-center gap-2">
                   <i className="fa-solid fa-calendar-check text-emerald-300"></i> {new Date(user.createdAt).toLocaleDateString()}
                 </p>
              </div>
            </div>
            
            <div className="mt-12 pt-8 border-t border-slate-50">
              <h3 className="text-[10px] font-bold text-slate-800 uppercase tracking-[0.3em] mb-8">账户偏好与系统权限</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                 {[
                   { icon: 'fa-shield-halved', label: '系统管理权限', status: user.role === 'ROLE_ADMIN' ? '已授权' : '无权限', color: user.role === 'ROLE_ADMIN' ? 'text-emerald-500' : 'text-slate-300' },
                   { icon: 'fa-bell', label: '即时教务通知', status: '开启', color: 'text-indigo-500' },
                   { icon: 'fa-earth-asia', label: '界面显示语言', status: '简体中文', color: 'text-indigo-500' },
                   { icon: 'fa-bolt', label: 'AI 功能实验室', status: '已解锁', color: 'text-purple-500' }
                 ].map((item, i) => (
                   <div key={i} className="flex items-center justify-between p-5 bg-white border border-slate-100 rounded-2xl hover:border-indigo-100 transition-all group">
                      <div className="flex items-center gap-4">
                        <div className="w-10 h-10 rounded-xl bg-slate-50 flex items-center justify-center text-slate-300 group-hover:bg-indigo-50 group-hover:text-indigo-500 transition-all">
                          <i className={`fa-solid ${item.icon} text-sm`}></i>
                        </div>
                        <span className="text-xs font-bold text-slate-600 tracking-tight">{item.label}</span>
                      </div>
                      <span className={`text-[10px] font-bold ${item.color} tracking-wider`}>{item.status}</span>
                   </div>
                 ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfileCenter;
