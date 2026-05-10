export const fieldClass =
  "h-10 w-full rounded-lg border border-slate-600 bg-slate-800 px-3 text-slate-100 outline-none placeholder:text-slate-500 focus:border-teal-400 focus:ring-4 focus:ring-teal-400/15";

export const textareaClass =
  "w-full resize-y rounded-lg border border-slate-600 bg-slate-800 p-3 leading-6 text-slate-100 outline-none placeholder:text-slate-500 focus:border-teal-400 focus:ring-4 focus:ring-teal-400/15";

export const labelClass = "mb-2 block text-sm font-bold text-slate-400";

export function Field({ label, children, className = "" }) {
  return (
    <label className={className}>
      <span className={labelClass}>{label}</span>
      {children}
    </label>
  );
}

