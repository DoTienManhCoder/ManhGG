import { useState } from "react";
import { Check, Copy, Download, Lock, Pencil, Sparkles, Trash2, Unlock, X, ZoomIn } from "lucide-react";
import { Button } from "../../../components/ui/Button";
import { IconButton } from "../../../components/ui/IconButton";
import { ModalShell } from "../../../components/ui/ModalShell";
import { generateListingTemplate } from "../../../services/roomsApi";
import { MediaPreview } from "./media/MediaPreview";
import { RoomMetaItem } from "./RoomMetaItem";

export function RoomDetail({ room, canManage, onClose, onEdit, onDelete, onToggleLock }) {
  const [downloadingMediaId, setDownloadingMediaId] = useState(null);
  const [downloadError, setDownloadError] = useState("");
  const [previewImage, setPreviewImage] = useState(null);
  const [listingTemplate, setListingTemplate] = useState("");
  const [isGeneratingTemplate, setIsGeneratingTemplate] = useState(false);
  const [hasCopiedTemplate, setHasCopiedTemplate] = useState(false);
  const [isTogglingLock, setIsTogglingLock] = useState(false);
  const canSaveImages = room.status === "open";
  const isLocked = room.status === "lock";

  async function handleSaveImage(media) {
    setDownloadError("");
    setDownloadingMediaId(media.id);

    try {
      const response = await fetch(media.url);
      if (!response.ok) {
        throw new Error("download-failed");
      }

      const blob = await response.blob();
      const objectUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = objectUrl;
      link.download = downloadFileName(room, media);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 0);
    } catch {
      setDownloadError("Không thể lưu ảnh. Vui lòng thử lại.");
    } finally {
      setDownloadingMediaId(null);
    }
  }

  async function handleGenerateTemplate() {
    setIsGeneratingTemplate(true);
    setHasCopiedTemplate(false);
    try {
      const payload = await generateListingTemplate(room.id);
      setListingTemplate(payload.content || "");
    } catch (error) {
      window.alert(error.message);
      if (error.message.includes("Phiên đăng nhập")) {
        window.location.reload();
      }
    } finally {
      setIsGeneratingTemplate(false);
    }
  }

  async function handleCopyTemplate() {
    if (!listingTemplate) return;

    await navigator.clipboard.writeText(listingTemplate);
    setHasCopiedTemplate(true);
    window.setTimeout(() => setHasCopiedTemplate(false), 1800);
  }

  async function handleToggleLock() {
    if (!onToggleLock) return;

    setIsTogglingLock(true);
    try {
      await onToggleLock(room);
    } catch (error) {
      window.alert(error.message);
    } finally {
      setIsTogglingLock(false);
    }
  }

  return (
    <ModalShell size="wide">
      <article>
        <div className="mb-5 flex items-start justify-between gap-4 max-sm:mb-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-300">Chi tiết phòng</p>
            <h2 className="text-2xl font-extrabold">{room.code}</h2>
          </div>
          <IconButton type="button" onClick={onClose} aria-label="Đóng">
            <X size={18} />
          </IconButton>
        </div>

        <div className="grid gap-4 xl:grid-cols-[minmax(0,1.3fr)_minmax(360px,0.7fr)]">
          <section className="grid content-start gap-4">
            <div className="grid grid-cols-3 gap-2.5 max-lg:grid-cols-2 max-sm:grid-cols-1">
              <RoomMetaItem label="Địa chỉ ẩn" value={room.address} />
              {room.realAddress && <RoomMetaItem label="Địa chỉ đúng" value={room.realAddress} />}
              <RoomMetaItem label="Giá" value={room.price} />
              <RoomMetaItem label="Trạng thái" value={room.status} />
              {room.layout && <RoomMetaItem label="Dạng phòng" value={room.layout} />}
              {room.furniture && <RoomMetaItem label="Nội thất" value={room.furniture} />}
              {room.area && <RoomMetaItem label="Diện tích" value={room.area} />}
            </div>

            <div className="grid grid-cols-[repeat(auto-fill,minmax(190px,1fr))] gap-3 max-sm:grid-cols-2 max-[420px]:grid-cols-1">
              {room.media?.length ? (
                room.media.map((media) => (
                  <div className="group relative aspect-square overflow-hidden rounded-lg border border-slate-700 bg-slate-800" key={media.id}>
                    {isImage(media) ? (
                      <button
                        className="block h-full w-full"
                        type="button"
                        onClick={() => setPreviewImage(media)}
                        aria-label={`Xem lớn ảnh ${media.name || room.code}`}
                      >
                        <img className="h-full w-full object-cover" src={media.url} alt={media.name || "Ảnh phòng"} />
                        <span className="absolute left-2 top-2 grid h-9 w-9 place-items-center rounded-lg bg-slate-950/85 text-slate-100 shadow-sm transition group-hover:bg-slate-900">
                          <ZoomIn size={17} />
                        </span>
                      </button>
                    ) : (
                      <MediaPreview media={media} controls />
                    )}
                    {canSaveImages && isImage(media) && (
                      <button
                        className="absolute bottom-2 right-2 inline-flex min-h-10 items-center justify-center gap-1.5 rounded-lg bg-slate-950/85 px-3 text-xs font-extrabold text-white shadow-lg transition hover:bg-slate-950 disabled:cursor-progress disabled:opacity-75 max-sm:left-2 max-sm:text-sm"
                        type="button"
                        onClick={() => handleSaveImage(media)}
                        disabled={downloadingMediaId === media.id}
                        title="Lưu ảnh về máy"
                        aria-label={`Lưu ảnh ${media.name || room.code} về máy`}
                      >
                        <Download size={16} />
                        {downloadingMediaId === media.id ? "Đang lưu" : "Lưu ảnh"}
                      </button>
                    )}
                  </div>
                ))
              ) : (
                <div className="grid aspect-square place-items-center rounded-lg border border-slate-700 bg-slate-800 font-bold text-slate-400">
                  Chưa có ảnh/video
                </div>
              )}
            </div>
            {downloadError && <p className="mt-2.5 text-sm font-bold text-red-300">{downloadError}</p>}

            <section className="rounded-lg border border-teal-800 bg-teal-950/40 p-4">
              <div className="flex items-center gap-2.5 max-sm:flex-col max-sm:items-stretch">
                <div className="min-w-0 flex-1">
                  <h3 className="font-extrabold text-teal-100">Mẫu tin thu hút khách</h3>
                  <p className="mt-1 text-sm font-semibold text-teal-200/80">
                    Dùng liên hệ của Mạnh để viết tin.
                  </p>
                </div>
                <Button type="button" onClick={handleGenerateTemplate} disabled={isGeneratingTemplate}>
                  <Sparkles size={16} />
                  {isGeneratingTemplate ? "Đang tạo..." : listingTemplate ? "Tạo mẫu mới" : "Tạo mẫu"}
                </Button>
              </div>

              {listingTemplate && (
                <div className="mt-3.5 rounded-lg border border-teal-800 bg-slate-950/70 p-3">
                  <pre className="whitespace-pre-wrap font-sans text-sm leading-6 text-slate-100">
                    {listingTemplate}
                  </pre>
                  <div className="mt-3 flex justify-end">
                    <Button variant="ghost" type="button" onClick={handleCopyTemplate}>
                      {hasCopiedTemplate ? <Check size={16} /> : <Copy size={16} />}
                      {hasCopiedTemplate ? "Đã copy" : "Copy"}
                    </Button>
                  </div>
                </div>
              )}
            </section>

            {canManage && (
              <div className="flex items-center gap-2.5 max-sm:flex-col max-sm:items-stretch">
                <Button variant="danger" type="button" onClick={() => onDelete(room)}>
                  <Trash2 size={16} />
                  Xóa phòng
                </Button>
                <Button variant="ghost" type="button" onClick={handleToggleLock} disabled={isTogglingLock}>
                  {isLocked ? <Unlock size={16} /> : <Lock size={16} />}
                  {isTogglingLock ? "Đang lưu..." : isLocked ? "Unlock phòng" : "Lock phòng"}
                </Button>
                <div className="flex-1" />
                <Button variant="ghost" type="button" onClick={() => onEdit(room)}>
                  <Pencil size={16} />
                  Sửa thông tin
                </Button>
              </div>
            )}
          </section>

          <aside className="grid content-start gap-4">
            <section className="rounded-lg border border-slate-700 bg-slate-800 p-3">
              <strong className="mb-2 block text-xs uppercase text-slate-400">Note nội bộ</strong>
              <pre className="whitespace-pre-wrap font-sans leading-6 text-slate-100">
                {room.note || "Chưa có note nội bộ."}
              </pre>
            </section>
          </aside>
        </div>
      </article>

      {previewImage && (
        <div className="fixed inset-0 z-20 grid place-items-center bg-slate-950/90 p-4 max-sm:p-2" role="dialog" aria-modal="true">
          <button
            className="absolute inset-0"
            type="button"
            onClick={() => setPreviewImage(null)}
            aria-label="Đóng ảnh phóng to"
          />
          <div className="relative z-10 flex max-h-[94vh] w-full max-w-6xl flex-col gap-3">
            <div className="flex items-center justify-end gap-2">
              {canSaveImages && (
                <button
                  className="inline-flex min-h-10 items-center justify-center gap-1.5 rounded-lg bg-slate-800 px-3 text-sm font-extrabold text-slate-100 shadow-lg transition hover:bg-slate-700 disabled:cursor-progress disabled:opacity-75"
                  type="button"
                  onClick={() => handleSaveImage(previewImage)}
                  disabled={downloadingMediaId === previewImage.id}
                >
                  <Download size={16} />
                  {downloadingMediaId === previewImage.id ? "Đang lưu" : "Lưu ảnh"}
                </button>
              )}
              <IconButton className="border-slate-600 bg-slate-800 text-slate-100 hover:bg-slate-700" type="button" onClick={() => setPreviewImage(null)} aria-label="Đóng">
                <X size={18} />
              </IconButton>
            </div>
            <div className="grid min-h-0 flex-1 place-items-center overflow-hidden rounded-lg bg-black">
              <img className="max-h-[calc(94vh-64px)] w-full object-contain" src={previewImage.url} alt={previewImage.name || "Ảnh phòng"} />
            </div>
          </div>
        </div>
      )}
    </ModalShell>
  );
}

function isImage(media) {
  return media.type?.startsWith("image/");
}

function downloadFileName(room, media) {
  const extension = fileExtension(media);
  const baseName = `${room.code || "phong"}-${media.name || media.id || "anh"}`.replace(/\.[^/.]+$/, "");
  const safeName = baseName
    .trim()
    .replace(/\s+/g, "-")
    .replace(/[^a-zA-Z0-9._-]/g, "")
    .replace(/-+/g, "-");

  return `${safeName || "anh-phong"}${extension}`;
}

function fileExtension(media) {
  const nameExtension = media.name?.match(/\.[a-zA-Z0-9]+$/)?.[0];
  if (nameExtension) return nameExtension;

  const typeExtension = media.type?.split("/")[1]?.split(";")[0];
  if (typeExtension === "jpeg") return ".jpg";
  if (typeExtension === "svg+xml") return ".svg";

  return typeExtension ? `.${typeExtension}` : ".jpg";
}
