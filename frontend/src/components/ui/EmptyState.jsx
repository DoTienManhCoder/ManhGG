export function EmptyState({ title, message }) {
  return (
    <section className="rounded-lg border border-dashed border-slate-600 bg-slate-800 p-8 text-center">
      {title && <h2 className="mb-2 text-xl font-extrabold">{title}</h2>}
      <p className="mb-0 text-slate-400">{message}</p>
    </section>
  );
}
