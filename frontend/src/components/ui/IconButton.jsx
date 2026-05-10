import { cn } from "../../lib/cn";

export function IconButton({ className, ...props }) {
  return (
    <button
      className={cn(
        "inline-grid h-10 w-10 place-items-center rounded-lg border border-slate-600 bg-slate-800 text-slate-100 transition hover:bg-slate-700",
        className,
      )}
      {...props}
    />
  );
}
