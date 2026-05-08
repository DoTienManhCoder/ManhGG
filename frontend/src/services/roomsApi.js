import { authHeaders } from "./authApi";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

function mediaUrl(path) {
  if (!path) return "";
  if (path.startsWith("http")) return path;
  return `${API_BASE_URL}${path}`;
}

export async function fetchRooms() {
  const response = await fetch(`${API_BASE_URL}/api/rooms`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("Khong the tai danh sach phong");

  const rooms = await response.json();
  return rooms.map(normalizeRoom);
}

export async function saveRoom(room, files, keepMediaIds) {
  const formData = new FormData();
  formData.append("address", room.address);
  formData.append("price", room.price);
  formData.append("code", room.code);
  formData.append("status", room.id ? room.status : "open");
  formData.append("note", room.note || "");
  formData.append("keepMediaIds", keepMediaIds.join(","));

  files.forEach((file) => formData.append("files", file));

  const response = await fetch(`${API_BASE_URL}/api/rooms${room.id ? `/${room.id}` : ""}`, {
    method: room.id ? "PUT" : "POST",
    headers: authHeaders(),
    body: formData,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Khong the luu phong");
  }

  return normalizeRoom(await response.json());
}

export async function deleteRoom(id) {
  const response = await fetch(`${API_BASE_URL}/api/rooms/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("Khong the xoa phong");
}

function normalizeRoom(room) {
  return {
    ...room,
    media: (room.media || []).map((item) => ({ ...item, url: mediaUrl(item.url) })),
  };
}
