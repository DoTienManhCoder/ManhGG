export const fieldClass =
  "h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-slate-900 outline-none focus:border-teal-700 focus:ring-4 focus:ring-teal-700/15";

export const textareaClass =
  "w-full resize-y rounded-lg border border-slate-200 bg-white p-3 leading-6 text-slate-900 outline-none focus:border-teal-700 focus:ring-4 focus:ring-teal-700/15";

export const labelClass = "mb-2 block text-sm font-bold text-slate-500";

export function Field({ label, children, className = "" }) {
  return (
    <label className={className}>
      <span className={labelClass}>{label}</span>
      {children}
    </label>
  );
}
