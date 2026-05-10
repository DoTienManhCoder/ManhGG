import { cn } from "../../lib/cn";

const variants = {
  primary: "border-teal-500 bg-teal-500 text-slate-950 hover:bg-teal-400",
  ghost: "border-slate-600 bg-slate-800 text-slate-100 hover:bg-slate-700",
  danger: "border-red-500 bg-slate-800 text-red-300 hover:bg-red-950/50",
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
