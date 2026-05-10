import { X } from "lucide-react";
import { MediaPreview } from "./MediaPreview";

export function MediaTile({ media, onRemove }) {
  return (
    <div className="relative aspect-square overflow-hidden rounded-lg border border-slate-700 bg-slate-800">
      <MediaPreview media={media} controls />
      <button
        className="absolute right-1.5 top-1.5 grid h-8 w-8 place-items-center rounded-full bg-slate-900/80 text-white"
        type="button"
        onClick={onRemove}
        aria-label={`Xóa ${media.name}`}
      >
        <X size={16} />
      </button>
    </div>
  );
}
