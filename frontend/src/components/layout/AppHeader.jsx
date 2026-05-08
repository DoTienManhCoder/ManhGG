import { CirclePlus, LogOut, Users } from "lucide-react";
import { Button } from "../ui/Button";

export function AppHeader({ user, isAdmin, onCreateRoom, onLogout, onManageUsers }) {
  return (
    <header className="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 py-5 max-md:flex-col max-md:items-stretch sm:px-8 lg:px-11">
      <div>
        <p className="mb-1 text-xs font-extrabold uppercase text-teal-700">ManhGG</p>
        <h1 className="text-3xl font-extrabold leading-tight text-slate-900 sm:text-4xl lg:text-5xl">
          MANHGG
        </h1>
      </div>
      <div className="flex flex-wrap items-center justify-end gap-2.5 max-md:justify-start">
        {user && <span className="text-sm font-bold text-slate-500">{user.username}</span>}
        {isAdmin && (
          <>
            <Button variant="ghost" type="button" onClick={onManageUsers}>
              <Users size={18} />
              Tai khoan
            </Button>
            <Button type="button" onClick={onCreateRoom}>
              <CirclePlus size={18} />
              Them phong
            </Button>
          </>
        )}
        <Button variant="ghost" type="button" onClick={onLogout}>
          <LogOut size={18} />
          Dang xuat
        </Button>
      </div>
    </header>
  );
}
