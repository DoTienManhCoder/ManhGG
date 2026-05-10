import { KeyRound, X } from "lucide-react";
import { useState } from "react";
import { Button } from "../../../components/ui/Button";
import { Field, fieldClass } from "../../../components/ui/Field";
import { IconButton } from "../../../components/ui/IconButton";
import { ModalShell } from "../../../components/ui/ModalShell";
import { login } from "../../../services/authApi";

export function LoginForm({ onClose, onLoggedIn }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    try {
      const user = await login(username, password);
      onLoggedIn(user);
    } catch (error) {
      window.alert(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <ModalShell>
      <form onSubmit={handleSubmit}>
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-300">Quản trị</p>
            <h2 className="text-2xl font-extrabold">Đăng nhập</h2>
          </div>
          <IconButton type="button" onClick={onClose} aria-label="Đóng">
            <X size={18} />
          </IconButton>
        </div>

        <div className="grid gap-3.5">
          <Field label="Tài khoản">
            <input
              className={fieldClass}
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoFocus
              required
            />
          </Field>
          <Field label="Mật khẩu">
            <input
              className={fieldClass}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              type="password"
              required
            />
          </Field>
        </div>

        <div className="mt-5 flex justify-end gap-2.5 max-sm:flex-col">
          <Button variant="ghost" type="button" onClick={onClose}>
            Hủy
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            <KeyRound size={16} />
            {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
          </Button>
        </div>
      </form>
    </ModalShell>
  );
}
