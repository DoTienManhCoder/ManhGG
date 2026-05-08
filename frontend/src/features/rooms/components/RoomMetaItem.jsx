export function RoomMetaItem({ label, value }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-slate-100 p-3">
      <strong className="mb-1 block text-xs uppercase text-slate-500">{label}</strong>
      <span>{value}</span>
    </div>
  );
}
