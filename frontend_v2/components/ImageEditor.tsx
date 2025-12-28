
import React, { useState, useRef } from 'react';
import { editImageWithGemini } from '../services/geminiService';
import { HistoryItem } from '../types';

const LOADING_MESSAGES = [
  "正在梳理图像逻辑...",
  "调配艺术色彩...",
  "正在施展智能魔法...",
  "即将呈现..."
];

const ImageEditor: React.FC = () => {
  const [image, setImage] = useState<string | null>(null);
  const [editedImage, setEditedImage] = useState<string | null>(null);
  const [prompt, setPrompt] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [loadingMsg, setLoadingMsg] = useState("");
  const [history, setHistory] = useState<HistoryItem[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleEdit = async () => {
    if (!image || !prompt) return;
    setIsLoading(true);
    let msgIndex = 0;
    const msgInterval = setInterval(() => {
      setLoadingMsg(LOADING_MESSAGES[msgIndex % LOADING_MESSAGES.length]);
      msgIndex++;
    }, 2000);

    try {
      const result = await editImageWithGemini(image, prompt);
      if (result) {
        setEditedImage(result);
        const newItem: HistoryItem = { id: Date.now().toString(), originalImage: image, editedImage: result, prompt, timestamp: Date.now() };
        setHistory(prev => [newItem, ...prev]);
      }
    } catch (err) {
      alert("AI 创作遇到了点麻烦，请重试。");
    } finally {
      clearInterval(msgInterval);
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 md:p-10 animate-in slide-in-from-bottom-2 duration-700">
      <div className="mb-8">
        <h1 className="text-lg font-bold text-slate-800 tracking-tight">AI 创意实验室</h1>
        <p className="text-slate-400 text-xs mt-1 font-medium">用简单的指令为您的课程海报增添无限可能。</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-[2rem] border border-slate-50 soft-shadow overflow-hidden flex flex-col min-h-[480px]">
            <div className="flex-1 relative flex items-center justify-center bg-slate-50/20 p-8">
              {!image ? (
                <div 
                  onClick={() => fileInputRef.current?.click()}
                  className="w-full h-72 border border-dashed border-slate-200 rounded-[1.5rem] flex flex-col items-center justify-center cursor-pointer hover:border-indigo-300 transition-all bg-white group"
                >
                  <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                    <i className="fa-solid fa-feather text-indigo-400 text-sm"></i>
                  </div>
                  <span className="text-slate-700 font-semibold text-xs">点击上传素材图片</span>
                  <p className="text-slate-400 text-[9px] mt-1.5 uppercase tracking-widest font-bold">支持 PNG, JPG, WEBP</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
                  <div className="space-y-2">
                    <span className="text-[9px] font-bold text-slate-300 uppercase tracking-[0.2em] ml-1">Original / 原图</span>
                    <div className="rounded-2xl w-full h-72 overflow-hidden shadow-sm">
                      <img src={image} className="w-full h-full object-cover grayscale-[0.1]" />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <span className="text-[9px] font-bold text-indigo-300 uppercase tracking-[0.2em] ml-1">AI Creative / 创作</span>
                    <div className="rounded-2xl w-full h-72 bg-white flex items-center justify-center soft-shadow relative overflow-hidden">
                      {isLoading ? (
                        <div className="flex flex-col items-center gap-3">
                          <div className="w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                          <p className="text-[9px] text-indigo-500 font-bold animate-pulse tracking-widest uppercase">{loadingMsg}</p>
                        </div>
                      ) : editedImage ? (
                        <img src={editedImage} className="w-full h-full object-cover animate-in fade-in zoom-in duration-700" />
                      ) : (
                        <p className="text-slate-300 text-[9px] font-bold uppercase tracking-[0.2em]">等待指令中</p>
                      )}
                    </div>
                  </div>
                </div>
              )}
              <input type="file" ref={fileInputRef} onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  const reader = new FileReader();
                  reader.onload = (ev) => setImage(ev.target?.result as string);
                  reader.readAsDataURL(file);
                }
              }} className="hidden" accept="image/*" />
            </div>

            <div className="p-5 bg-white border-t border-slate-50">
              <div className="flex flex-col sm:flex-row gap-3">
                <input 
                  type="text"
                  value={prompt}
                  onChange={(e) => setPrompt(e.target.value)}
                  placeholder="例如：'让光效更柔和' 或 '变成 3D 渲染风格'..."
                  className="flex-1 px-4 py-3 bg-slate-50 border border-transparent rounded-xl focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium"
                />
                <button
                  onClick={handleEdit}
                  disabled={!image || !prompt || isLoading}
                  className="px-6 py-3 gradient-bg text-white rounded-xl text-[11px] font-bold shadow-lg shadow-indigo-500/10 hover:scale-[1.02] active:scale-95 disabled:opacity-30 transition-all whitespace-nowrap"
                >
                  {isLoading ? '生成中' : '立即执行'}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-[2rem] border border-slate-50 p-6 soft-shadow flex flex-col">
          <h3 className="text-[10px] font-bold text-slate-800 mb-6 flex items-center gap-2 tracking-[0.2em] uppercase">
            <i className="fa-solid fa-history text-indigo-300"></i>
            创作历史
          </h3>
          <div className="space-y-4 flex-1 overflow-y-auto pr-1 hide-scrollbar">
            {history.length === 0 ? (
              <div className="text-center py-16 bg-slate-50/50 rounded-2xl border border-dashed border-slate-100">
                <p className="text-slate-300 text-[9px] font-bold uppercase tracking-widest italic">暂无记录</p>
              </div>
            ) : (
              history.map(item => (
                <div key={item.id} className="group p-2.5 rounded-xl border border-slate-50 hover:border-indigo-100 transition-all bg-white hover:shadow-md cursor-pointer"
                  onClick={() => { setEditedImage(item.editedImage); setImage(item.originalImage); }}>
                  <div className="flex gap-3">
                    <img src={item.editedImage} className="w-12 h-12 rounded-lg object-cover" />
                    <div className="flex-1 min-w-0 flex flex-col justify-center">
                      <p className="text-[10px] font-bold text-slate-600 truncate">{item.prompt}</p>
                      <p className="text-[8px] text-slate-300 mt-1 font-bold uppercase tracking-tighter">
                        {new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImageEditor;
