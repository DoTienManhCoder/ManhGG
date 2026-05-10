import { DoorOpen, Lock } from "lucide-react";
import { MediaPreview } from "./media/MediaPreview";

export function RoomCard({ room, onOpen }) {
  const firstMedia = room.media?.[0];
  const isOpen = room.status === "open";

  return (
    <button
      className="relative flex h-full min-h-72 flex-col overflow-hidden rounded-lg border border-slate-700 bg-slate-800 text-left shadow-sm shadow-black/20 transition hover:border-teal-400/60"
      type="button"
      onClick={onOpen}
    >
      <div className="grid aspect-square w-full shrink-0 place-items-center overflow-hidden bg-slate-800 font-bold text-slate-400">
        {firstMedia ? <MediaPreview media={firstMedia} /> : <span>Chưa có ảnh</span>}
      </div>
      <span
        className={`absolute right-2.5 top-2.5 inline-flex min-h-9 items-center gap-1.5 rounded-full border px-3 text-sm font-black uppercase tracking-wide shadow-lg ${
          isOpen ? "border-emerald-300 bg-emerald-600 text-white" : "border-red-300 bg-red-600 text-white"
        }`}
      >
        {isOpen ? <DoorOpen size={16} /> : <Lock size={16} />}
        {isOpen ? "OPEN" : "LOCK"}
      </span>
      <div className="grid gap-1.5 p-3">
        <strong className="line-clamp-2 text-lg leading-6">{room.realAddress || room.address}</strong>
        <span className="text-sm font-extrabold text-slate-400">{room.code}</span>
        <b className="text-teal-300">{room.price}</b>
        <p className="line-clamp-2 min-h-10 text-sm leading-5 text-slate-400">{room.address}</p>
      </div>
    </button>
  );
}
