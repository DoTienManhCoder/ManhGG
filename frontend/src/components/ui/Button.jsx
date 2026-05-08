import { cn } from "../../lib/cn";

const variants = {
  primary: "border-teal-700 bg-teal-700 text-white hover:bg-teal-800",
  ghost: "border-slate-200 bg-white text-slate-900 hover:bg-slate-50",
  danger: "border-red-600 bg-white text-red-600 hover:bg-red-50",
};

export function Button({ className, variant = "primary", ...props }) {
  return (
    <button
      className={cn(
        "inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border px-4 text-sm font-bold transition disabled:pointer-events-none",
        variants[variant],
        className,
      )}
      {...props}
    />
  );
}
