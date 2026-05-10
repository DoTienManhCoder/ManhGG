import { ImagePlus, X } from "lucide-react";
import { useState } from "react";
import { Button } from "../../../components/ui/Button";
import { Field, fieldClass, labelClass, textareaClass } from "../../../components/ui/Field";
import { IconButton } from "../../../components/ui/IconButton";
import { ModalShell } from "../../../components/ui/ModalShell";
import { saveRoom } from "../../../services/roomsApi";
import { EMPTY_ROOM, ROOM_STATUSES } from "../constants";
import { MediaTile } from "./media/MediaTile";

export function RoomForm({ room, onClose, onSaved }) {
  const [form, setForm] = useState({ ...EMPTY_ROOM, ...room, media: undefined });
  const [existingMedia, setExistingMedia] = useState(room.media || []);
  const [newFiles, setNewFiles] = useState([]);
  const [isSaving, setIsSaving] = useState(false);
  const isEditing = Boolean(form.id);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function closeForm() {
    newFiles.forEach((item) => URL.revokeObjectURL(item.url));
    onClose();
  }

  function addFiles(files, inputElement) {
    const prepared = Array.from(files).map((file) => ({
      id: crypto.randomUUID(),
      file,
      name: file.name,
      type: file.type,
      url: URL.createObjectURL(file),
    }));

    setNewFiles((current) => [...current, ...prepared]);
    if (inputElement) inputElement.value = "";
  }

  function removeNewFile(id) {
    setNewFiles((current) => {
      const target = current.find((item) => item.id === id);
      if (target) URL.revokeObjectURL(target.url);
      return current.filter((item) => item.id !== id);
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    try {
      await saveRoom(
        form,
        newFiles.map((item) => item.file),
        existingMedia.map((item) => item.id),
      );
      newFiles.forEach((item) => URL.revokeObjectURL(item.url));
      await onSaved();
    } catch (error) {
      window.alert(error.message);
      if (error.message.includes("Phiên đăng nhập")) {
        window.location.reload();
      }
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ModalShell>
      <form onSubmit={handleSubmit}>
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-300">Thông tin phòng</p>
            <h2 className="text-2xl font-extrabold">{form.id ? "Sửa phòng" : "Thêm phòng"}</h2>
          </div>
          <IconButton type="button" onClick={closeForm} aria-label="Đóng">
            <X size={18} />
          </IconButton>
        </div>

        <section className="rounded-lg border border-slate-700 bg-slate-800 p-4">
          <h3 className="mb-3 text-sm font-extrabold uppercase text-slate-300">Thông tin thật</h3>
          <div className="grid grid-cols-2 gap-3.5 max-sm:grid-cols-1">
            <Field label="Địa chỉ thật">
              <input
                className={fieldClass}
                value={form.realAddress}
                onChange={(event) => updateField("realAddress", event.target.value)}
                placeholder="Địa chỉ chính xác để quản lý nội bộ"
                required
              />
            </Field>
            <Field label="Mã phòng">
              <input
                className={fieldClass}
                value={form.code}
                onChange={(event) => updateField("code", event.target.value)}
                required
              />
            </Field>
            <Field label="Giá">
              <input
                className={fieldClass}
                value={form.price}
                onChange={(event) => updateField("price", event.target.value)}
                required
              />
            </Field>
            {isEditing && (
              <Field label="Trạng thái">
                <select
                  className={fieldClass}
                  value={form.status}
                  onChange={(event) => updateField("status", event.target.value)}
                >
                  {ROOM_STATUSES.map((status) => (
                    <option key={status} value={status}>
                      {status === "open" ? "Open" : "Lock"}
                    </option>
                  ))}
                </select>
              </Field>
            )}
          </div>
        </section>

        <section className="mt-4 rounded-lg border border-teal-800 bg-teal-950/40 p-4">
          <h3 className="mb-3 text-sm font-extrabold uppercase text-teal-200">Thông tin tạo prompt</h3>
          <div className="grid grid-cols-2 gap-3.5 max-sm:grid-cols-1">
            <Field label="Địa chỉ">
              <input
                className={fieldClass}
                value={form.address}
                onChange={(event) => updateField("address", event.target.value)}
                placeholder="Địa chỉ ẩn/khu vực được phép gửi khách"
                required
              />
            </Field>
            <Field label="Dạng phòng">
              <input
                className={fieldClass}
                value={form.layout}
                onChange={(event) => updateField("layout", event.target.value)}
                placeholder="Ví dụ: Studio, 1PN, 2PN, phòng gác..."
              />
            </Field>
            <Field label="Nội thất">
              <input
                className={fieldClass}
                value={form.furniture}
                onChange={(event) => updateField("furniture", event.target.value)}
                placeholder="Ví dụ: full nội thất, máy lạnh, máy giặt"
              />
            </Field>
          </div>

          <label className="mt-3.5 block">
            <span className={labelClass}>Tiện ích / vị trí xung quanh</span>
            <textarea
              className={textareaClass}
              value={form.amenities}
              onChange={(event) => updateField("amenities", event.target.value)}
              rows={4}
              placeholder="Gần chợ, siêu thị, bến xe, trường học, bảo vệ, thang máy..."
            />
          </label>

          <label className="mt-3.5 block">
            <span className={labelClass}>Điểm nổi bật</span>
            <textarea
              className={textareaClass}
              value={form.sellingPoints}
              onChange={(event) => updateField("sellingPoints", event.target.value)}
              rows={4}
              placeholder="Phòng mới, thoáng, view đẹp, được nuôi thú cưng, vào ở ngay..."
            />
          </label>
        </section>

        <label className="mt-4 block rounded-lg border border-slate-700 bg-slate-800 p-4">
          <span className={labelClass}>Note thông tin toàn bộ (không đưa vào prompt)</span>
          <textarea
            className={textareaClass}
            value={form.note}
            onChange={(event) => updateField("note", event.target.value)}
            rows={6}
            placeholder="Lưu tất cả thông tin nội bộ: điểm không tốt, chủ nhà, phí phát sinh, địa chỉ chi tiết, lưu ý khi tư vấn..."
          />
        </label>

        <label className="mt-3.5 inline-flex min-h-10 items-center gap-2 rounded-lg border border-slate-700 bg-slate-800 px-4 font-bold text-slate-100 hover:bg-slate-700">
          <ImagePlus size={18} />
          <span>Chọn ảnh/video</span>
          <input
            className="hidden"
            type="file"
            accept="image/*,video/*"
            multiple
            onChange={(event) => addFiles(event.target.files, event.target)}
          />
        </label>

        <div className="mt-3.5 grid grid-cols-[repeat(auto-fill,minmax(140px,1fr))] gap-2.5">
          {existingMedia.map((media) => (
            <MediaTile
              key={media.id}
              media={media}
              onRemove={() => setExistingMedia((current) => current.filter((item) => item.id !== media.id))}
            />
          ))}
          {newFiles.map((media) => (
            <MediaTile key={media.id} media={media} onRemove={() => removeNewFile(media.id)} />
          ))}
        </div>

        <div className="mt-5 flex items-center justify-end gap-2.5 max-sm:flex-col max-sm:items-stretch">
          <Button variant="ghost" type="button" onClick={closeForm}>
            Hủy
          </Button>
          <Button type="submit" disabled={isSaving}>
            {isSaving ? "Đang lưu..." : "Lưu phòng"}
          </Button>
        </div>
      </form>
    </ModalShell>
  );
}
