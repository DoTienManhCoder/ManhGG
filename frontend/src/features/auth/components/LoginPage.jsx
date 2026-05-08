import { KeyRound } from "lucide-react";
import { useState } from "react";
import { Button } from "../../../components/ui/Button";
import { Field, fieldClass } from "../../../components/ui/Field";
import { login } from "../../../services/authApi";

export function LoginPage({ onLoggedIn }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    try {
      onLoggedIn(await login(username, password));
    } catch (error) {
      window.alert(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-[radial-gradient(circle_at_top_left,#ccfbf1,transparent_34%),linear-gradient(135deg,#f8fafc,#e2e8f0)] px-4 py-10">
      <section className="grid w-[min(960px,100%)] grid-cols-[1.05fr_0.95fr] overflow-hidden rounded-lg border border-white/70 bg-white shadow-2xl max-md:grid-cols-1">
        <div className="flex min-h-[560px] flex-col justify-between bg-slate-950 p-8 text-white max-md:min-h-80">
          <div className="flex items-center gap-3">
            <div className="grid size-14 place-items-center rounded-lg bg-teal-400 text-3xl font-black text-slate-950 shadow-lg shadow-teal-400/30">
              M
            </div>
            <div>
              <p className="text-xs font-extrabold uppercase text-teal-200">Private access</p>
              <strong className="text-2xl font-black tracking-normal">MANHGG</strong>
            </div>
          </div>

          <div>
            <h1 className="max-w-md text-5xl font-black leading-tight max-sm:text-4xl">MANHGG</h1>
            <p className="mt-4 max-w-sm text-base font-bold leading-7 text-slate-300">
              Đăng nhâp để trải nghiệm hệ thống quản lý phòng trọ MANHGG.
            </p>
          </div>

          <div className="h-2 w-28 rounded-full bg-teal-400" />
        </div>

        <form className="flex flex-col justify-center p-8 sm:p-10" onSubmit={handleSubmit}>
          <div className="mb-8">
            <p className="mb-1 text-xs font-extrabold uppercase text-teal-700">Login</p>
            <h2 className="font-['Roboto'] text-4xl font-black leading-tight text-slate-950">Vào hệ thống</h2>
          </div>

          <div className="grid gap-4">
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

          <Button className="mt-6 w-full" type="submit" disabled={isSubmitting}>
            <KeyRound size={18} />
            {isSubmitting ? "Đang đăng nhập..." : "Login"}
          </Button>
        </form>
      </section>
    </main>
  );
}
