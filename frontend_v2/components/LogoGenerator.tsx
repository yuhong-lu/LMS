
import React, { useState } from 'react';
import { generateLogoWithGemini } from '../services/geminiService';

const LogoGenerator: React.FC = () => {
  const [prompt, setPrompt] = useState("");
  const [result, setResult] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleGenerate = async () => {
    if (!prompt) return;
    setIsLoading(true);
    try {
      const img = await generateLogoWithGemini(prompt);
      setResult(img);
    } catch (err) {
      alert("生成失败，请稍后重试。");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 md:p-10 animate-in zoom-in duration-1000">
      <div className="mb-10 text-center">
        <h1 className="text-lg font-bold text-slate-800 tracking-widest uppercase mb-2">品牌资产中心</h1>
        <p className="text-slate-400 max-w-md mx-auto text-[11px] font-medium leading-relaxed">
          描述您对课程品牌的视觉愿景，AI 将为您具象化呈现高水准的艺术标识。
        </p>
      </div>

      <div className="bg-white rounded-[3rem] border border-slate-50 shadow-2xl overflow-hidden p-8 md:p-12 flex flex-col md:flex-row gap-12 items-center">
        <div className="flex-1 space-y-8 w-full">
          <div>
            <label className="block text-[10px] font-bold text-slate-400 mb-3 uppercase tracking-[0.2em]">视觉风格描述</label>
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="描述您的创意，例如：'极简主义的渐变几何体' 或 '代表流动智慧的丝绸质感波纹'..."
              className="w-full p-5 bg-slate-50/50 border border-transparent rounded-[1.5rem] h-36 focus:bg-white focus:border-indigo-100 outline-none transition-all text-xs font-medium leading-relaxed"
            />
          </div>
          <button
            onClick={handleGenerate}
            disabled={isLoading || !prompt}
            className="w-full py-4 gradient-bg text-white rounded-2xl font-bold text-xs tracking-widest uppercase shadow-xl shadow-indigo-500/20 hover:scale-[1.01] active:scale-95 disabled:opacity-30 transition-all flex items-center justify-center gap-3"
          >
            {isLoading ? (
              <>
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                正在激发灵感
              </>
            ) : '生成艺术标识'}
          </button>
        </div>

        <div className="w-full md:w-72 h-72 bg-slate-50/30 rounded-[2.5rem] border border-dashed border-slate-200 flex items-center justify-center relative overflow-hidden group">
          {result ? (
            <div className="p-6 w-full h-full animate-in fade-in zoom-in duration-1000">
                <img src={result} alt="Logo Result" className="w-full h-full object-contain drop-shadow-2xl" />
            </div>
          ) : (
            <div className="text-center p-8">
              <div className="w-12 h-12 rounded-2xl bg-white flex items-center justify-center mx-auto mb-4 shadow-sm">
                <i className="fa-solid fa-feather-pointed text-slate-200 text-lg"></i>
              </div>
              <p className="text-slate-300 text-[10px] font-bold uppercase tracking-widest">设计成果预览</p>
            </div>
          )}
          {isLoading && (
            <div className="absolute inset-0 bg-white/60 backdrop-blur-md flex items-center justify-center">
              <div className="flex flex-col items-center gap-3">
                <div className="w-10 h-10 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
                <p className="text-[9px] font-bold text-indigo-600 tracking-[0.3em] uppercase">Designing</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default LogoGenerator;
