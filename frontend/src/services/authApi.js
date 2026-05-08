const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
const TOKEN_KEY = "manhgg.authToken";

export function getAuthToken() {
  return window.localStorage.getItem(TOKEN_KEY);
}

function setAuthToken(token) {
  window.localStorage.setItem(TOKEN_KEY, token);
}

function clearAuthToken() {
  window.localStorage.removeItem(TOKEN_KEY);
}

export function authHeaders() {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function readError(response, fallback) {
  const message = await response.text();
  return message || fallback;
}

export async function login(username, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error(await readError(response, "Khong the dang nhap"));

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

  if (!response.ok) throw new Error(await readError(response, "Khong the tai tai khoan"));
  return response.json();
}

export async function createUser(username, password) {
  const response = await fetch(`${API_BASE_URL}/api/users`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error(await readError(response, "Khong the tao tai khoan"));
  return response.json();
}

export async function deleteUser(id) {
  const response = await fetch(`${API_BASE_URL}/api/users/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });

  if (!response.ok) throw new Error(await readError(response, "Khong the xoa tai khoan"));
}
