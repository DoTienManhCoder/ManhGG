import { Pencil, Trash2, X } from "lucide-react";
import { Button } from "../../../components/ui/Button";
import { IconButton } from "../../../components/ui/IconButton";
import { ModalShell } from "../../../components/ui/ModalShell";
import { MediaPreview } from "./media/MediaPreview";
import { RoomMetaItem } from "./RoomMetaItem";

export function RoomDetail({ room, canManage, onClose, onEdit, onDelete }) {
  return (
    <ModalShell>
      <article>
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-700">Chi tiet phong</p>
            <h2 className="text-2xl font-extrabold">{room.code}</h2>
          </div>
          <IconButton type="button" onClick={onClose} aria-label="Dong">
            <X size={18} />
          </IconButton>
        </div>

        <div className="grid grid-cols-3 gap-2.5 max-sm:grid-cols-1">
          <RoomMetaItem label="Dia chi" value={room.address} />
          <RoomMetaItem label="Gia" value={room.price} />
          <RoomMetaItem label="Trang thai" value={room.status} />
        </div>

        <div className="mt-3.5 grid grid-cols-[repeat(auto-fill,minmax(140px,1fr))] gap-2.5">
          {room.media?.length ? (
            room.media.map((media) => (
              <div className="aspect-square overflow-hidden rounded-lg border border-slate-200 bg-slate-100" key={media.id}>
                <MediaPreview media={media} controls />
              </div>
            ))
          ) : (
            <div className="grid aspect-square place-items-center rounded-lg border border-slate-200 bg-slate-100 font-bold text-slate-500">
              Chua co anh/video
            </div>
          )}
        </div>

        <pre className="mt-4 max-h-[34vh] overflow-auto whitespace-pre-wrap font-sans leading-6 text-slate-900">
          {room.note || "Chua co noi dung tu van."}
        </pre>

        {canManage && (
          <div className="mt-5 flex items-center gap-2.5 max-sm:flex-col max-sm:items-stretch">
            <Button variant="danger" type="button" onClick={() => onDelete(room)}>
              <Trash2 size={16} />
              Xoa phong
            </Button>
            <div className="flex-1" />
            <Button variant="ghost" type="button" onClick={() => onEdit(room)}>
              <Pencil size={16} />
              Sua thong tin
            </Button>
          </div>
        )}
      </article>
    </ModalShell>
  );
}
