
import React from 'react';

interface LogoProps {
  className?: string;
  iconOnly?: boolean;
}

const Logo: React.FC<LogoProps> = ({ className = "", iconOnly = false }) => {
  return (
    <div className={`flex items-center gap-2.5 select-none ${className}`}>
      <div className="w-8 h-8 rounded-xl gradient-bg flex items-center justify-center shadow-lg shadow-indigo-500/10 relative overflow-hidden group">
        <svg viewBox="0 0 24 24" className="w-4 h-4 text-white flow-animation" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M4 12c2-2 5-2 7 0s5 2 7 0" strokeLinecap="round" />
          <path d="M4 17c2-2 5-2 7 0s5 2 7 0" strokeLinecap="round" />
          <path d="M12 3c-2 3-2 6 0 9 2-3 2-6 0-9z" strokeLinejoin="round" />
        </svg>
      </div>
      {!iconOnly && (
        <div className="flex items-baseline">
          <span className="font-outfit font-extralight italic text-base tracking-tighter text-slate-500">Edu</span>
          <span className="font-script gradient-text text-xl ml-0.5 -rotate-1 transition-all group-hover:rotate-0 duration-700">Flow</span>
        </div>
      )}
    </div>
  );
};

export default Logo;
