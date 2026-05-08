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
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ModalShell>
      <form onSubmit={handleSubmit}>
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-700">Thong tin phong</p>
            <h2 className="text-2xl font-extrabold">{form.id ? "Sua phong" : "Them phong"}</h2>
          </div>
          <IconButton type="button" onClick={closeForm} aria-label="Dong">
            <X size={18} />
          </IconButton>
        </div>

        <div className="grid grid-cols-2 gap-3.5 max-sm:grid-cols-1">
          <Field label="Dia chi">
            <input
              className={fieldClass}
              value={form.address}
              onChange={(event) => updateField("address", event.target.value)}
              required
            />
          </Field>
          <Field label="Gia">
            <input
              className={fieldClass}
              value={form.price}
              onChange={(event) => updateField("price", event.target.value)}
              required
            />
          </Field>
          <Field label="Ma phong">
            <input
              className={fieldClass}
              value={form.code}
              onChange={(event) => updateField("code", event.target.value)}
              required
            />
          </Field>
          {isEditing && (
            <Field label="Trang thai">
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

        <label className="mt-3.5 block">
          <span className={labelClass}>Noi dung tu van / chinh sach</span>
          <textarea
            className={textareaClass}
            value={form.note}
            onChange={(event) => updateField("note", event.target.value)}
            rows={10}
            placeholder="Dan thong tin du an, chinh sach, noi that, hoa hong..."
          />
        </label>

        <label className="mt-3.5 inline-flex min-h-10 items-center gap-2 rounded-lg border border-slate-200 bg-slate-100 px-4 font-bold text-slate-900 hover:bg-slate-200">
          <ImagePlus size={18} />
          <span>Chon anh/video</span>
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
            Huy
          </Button>
          <Button type="submit" disabled={isSaving}>
            {isSaving ? "Dang luu..." : "Luu phong"}
          </Button>
        </div>
      </form>
    </ModalShell>
  );
}
