export function EmptyState({ title, message }) {
  return (
    <section className="rounded-lg border border-dashed border-slate-200 bg-white p-8 text-center">
      {title && <h2 className="mb-2 text-xl font-extrabold">{title}</h2>}
      <p className="mb-0 text-slate-500">{message}</p>
    </section>
  );
}
