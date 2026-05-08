import { Trash2, UserPlus, X } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "../../../components/ui/Button";
import { Field, fieldClass } from "../../../components/ui/Field";
import { IconButton } from "../../../components/ui/IconButton";
import { ModalShell } from "../../../components/ui/ModalShell";
import { createUser, deleteUser, fetchUsers } from "../../../services/authApi";

export function UserManager({ onClose }) {
  const [users, setUsers] = useState([]);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  async function reloadUsers() {
    setIsLoading(true);
    try {
      setUsers(await fetchUsers());
    } catch (error) {
      window.alert(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    reloadUsers();
  }, []);

  async function handleCreateUser(event) {
    event.preventDefault();
    setIsSaving(true);
    try {
      await createUser(username, password);
      setUsername("");
      setPassword("");
      await reloadUsers();
    } catch (error) {
      window.alert(error.message);
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteUser(user) {
    const confirmed = window.confirm(`Xoa tai khoan ${user.username}?`);
    if (!confirmed) return;

    try {
      await deleteUser(user.id);
      await reloadUsers();
    } catch (error) {
      window.alert(error.message);
    }
  }

  return (
    <ModalShell>
      <section>
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-700">Admin</p>
            <h2 className="text-2xl font-extrabold">Quan ly tai khoan</h2>
          </div>
          <IconButton type="button" onClick={onClose} aria-label="Dong">
            <X size={18} />
          </IconButton>
        </div>

        <form className="grid grid-cols-[1fr_1fr_auto] items-end gap-3.5 max-md:grid-cols-1" onSubmit={handleCreateUser}>
          <Field label="Tai khoan moi">
            <input
              className={fieldClass}
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </Field>
          <Field label="Mat khau">
            <input
              className={fieldClass}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              type="password"
              required
            />
          </Field>
          <Button type="submit" disabled={isSaving}>
            <UserPlus size={16} />
            {isSaving ? "Dang tao..." : "Them"}
          </Button>
        </form>

        <div className="mt-5 overflow-hidden rounded-lg border border-slate-200">
          {isLoading ? (
            <p className="p-4 font-bold text-slate-500">Dang tai tai khoan...</p>
          ) : (
            users.map((user) => (
              <div
                className="grid grid-cols-[1fr_auto_auto] items-center gap-3 border-b border-slate-200 p-4 last:border-b-0 max-sm:grid-cols-1"
                key={user.id}
              >
                <strong>{user.username}</strong>
                <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-extrabold text-slate-600">
                  {user.role}
                </span>
                <Button
                  variant="danger"
                  type="button"
                  onClick={() => handleDeleteUser(user)}
                  disabled={user.role === "ADMIN"}
                >
                  <Trash2 size={16} />
                  Xoa
                </Button>
              </div>
            ))
          )}
        </div>
      </section>
    </ModalShell>
  );
}
