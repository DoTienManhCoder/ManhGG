import { cn } from "../../lib/cn";

export function IconButton({ className, ...props }) {
  return (
    <button
      className={cn(
        "inline-grid h-10 w-10 place-items-center rounded-lg border border-slate-200 bg-slate-100 text-slate-900 transition hover:bg-slate-200",
        className,
      )}
      {...props}
    />
  );
}
