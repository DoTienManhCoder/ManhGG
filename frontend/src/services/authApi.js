const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
const TOKEN_KEY = "manhgg.authToken";
const COOKIE_MAX_AGE = 60 * 60 * 24 * 30;

export function getAuthToken() {
  return window.localStorage.getItem(TOKEN_KEY) || getCookie(TOKEN_KEY);
}

function setAuthToken(token) {
  window.localStorage.setItem(TOKEN_KEY, token);
  document.cookie = `${TOKEN_KEY}=${encodeURIComponent(token)}; Max-Age=${COOKIE_MAX_AGE}; Path=/; SameSite=Lax`;
}

export function clearAuthToken() {
  window.localStorage.removeItem(TOKEN_KEY);
  document.cookie = `${TOKEN_KEY}=; Max-Age=0; Path=/; SameSite=Lax`;
}

function getCookie(name) {
  const prefix = `${name}=`;
  const item = document.cookie
    .split(";")
    .map((value) => value.trim())
    .find((value) => value.startsWith(prefix));

  return item ? decodeURIComponent(item.slice(prefix.length)) : "";
}

export function authHeaders() {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function readApiError(response, fallback) {
  const rawMessage = await response.text();
  let message = rawMessage;

  try {
    const payload = JSON.parse(rawMessage);
    message = payload.message || payload.error || fallback;
  } catch {
    message = rawMessage || fallback;
  }

  if (response.status === 401) {
    clearAuthToken();
    return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
  }

  return message || fallback;
}

export async function login(username, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error(await readApiError(response, "Không thể đăng nhập"));

  const payload = await response.json();
  setAuthToken(payload.token);
  return payload.user;
}

export async function loadCurrentUser() {
  if (!getAuthToken()) return null;

  const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
    headers: authHeaders(),
  });

  if (!response.ok) {
    clearAuthToken();
    return null;
  }

  return response.json();
}

export async function logout() {
  if (getAuthToken()) {
    await fetch(`${API_BASE_URL}/api/auth/logout`, {
      method: "POST",
      headers: authHeaders(),
    }).catch(() => {});
  }

  clearAuthToken();
}

export async function fetchUsers() {
  const response = await fetch(`${API_BASE_URL}/api/users`, {
    headers: authHeaders(),
  });

  if (!response.ok) throw new Error(await readApiError(response, "Không thể tải tài khoản"));
  return response.json();
}

export async function createUser(username, password) {
  const response = await fetch(`${API_BASE_URL}/api/users`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error(await readApiError(response, "Không thể tạo tài khoản"));
  return response.json();
}

export async function deleteUser(id) {
  const response = await fetch(`${API_BASE_URL}/api/users/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });

  if (!response.ok) throw new Error(await readApiError(response, "Không thể xóa tài khoản"));
}
