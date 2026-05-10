import { cn } from "../../lib/cn";

const sizes = {
  default: "w-[min(920px,100%)]",
  wide: "w-[min(1280px,100%)]",
};

export function ModalShell({ children, size = "default", className }) {
  return (
    <div className="fixed inset-0 z-10 grid items-start justify-items-center overflow-auto bg-black/70 px-3 py-6 max-sm:py-3">
      <div className={cn("rounded-lg border border-slate-600 bg-slate-800 p-6 shadow-2xl shadow-black/40 max-sm:p-4", sizes[size], className)}>
        {children}
      </div>
    </div>
  );
}
