export function ModalShell({ children }) {
  return (
    <div className="fixed inset-0 z-10 grid items-start justify-items-center overflow-auto bg-slate-900/50 px-3 py-6">
      <div className="max-h-[calc(100vh-48px)] w-[min(920px,100%)] overflow-auto rounded-lg bg-white p-6 shadow-2xl">
        {children}
      </div>
    </div>
  );
}
