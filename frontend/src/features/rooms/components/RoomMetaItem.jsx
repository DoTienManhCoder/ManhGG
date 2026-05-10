export function RoomMetaItem({ label, value }) {
  const isStatus = label === "Trạng thái";
  const isOpen = value === "open";

  return (
    <div className="rounded-lg border border-slate-700 bg-slate-800 p-3">
      <strong className="mb-1 block text-xs uppercase text-slate-400">{label}</strong>
      {isStatus ? (
        <span
          className={`inline-flex min-h-8 items-center rounded-full px-3 text-sm font-black uppercase tracking-wide text-white ${
            isOpen ? "bg-emerald-600" : "bg-red-600"
          }`}
        >
          {isOpen ? "OPEN" : "LOCK"}
        </span>
      ) : (
        <span>{value}</span>
      )}
    </div>
  );
}
