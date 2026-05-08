import { DoorOpen, Lock } from "lucide-react";
import { MediaPreview } from "./media/MediaPreview";

export function RoomCard({ room, onOpen }) {
  const firstMedia = room.media?.[0];

  return (
    <button
      className="relative min-h-72 overflow-hidden rounded-lg border border-slate-200 bg-white text-left shadow-sm transition hover:border-teal-700/45"
      type="button"
      onClick={onOpen}
    >
      <div className="grid aspect-square w-full place-items-center bg-slate-100 font-bold text-slate-500">
        {firstMedia ? <MediaPreview media={firstMedia} /> : <span>Chua co anh</span>}
      </div>
      <span
        className={`absolute right-2.5 top-2.5 inline-flex min-h-8 items-center gap-1.5 rounded-full px-2.5 text-xs font-extrabold uppercase ${
          room.status === "open" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-600"
        }`}
      >
        {room.status === "open" ? <DoorOpen size={14} /> : <Lock size={14} />}
        {room.status}
      </span>
      <div className="grid gap-1.5 p-3">
        <strong className="text-lg">{room.code}</strong>
        <b className="text-teal-800">{room.price}</b>
        <p className="line-clamp-2 min-h-10 text-sm leading-5 text-slate-500">{room.address}</p>
      </div>
    </button>
  );
}
