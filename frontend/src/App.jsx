import { useEffect, useMemo, useState } from "react";
import { AppHeader } from "./components/layout/AppHeader";
import { EmptyState } from "./components/ui/EmptyState";
import { LoginPage } from "./features/auth/components/LoginPage";
import { UserManager } from "./features/auth/components/UserManager";
import { RoomDetail } from "./features/rooms/components/RoomDetail";
import { RoomForm } from "./features/rooms/components/RoomForm";
import { RoomGrid } from "./features/rooms/components/RoomGrid";
import { RoomToolbar } from "./features/rooms/components/RoomToolbar";
import { EMPTY_ROOM, ROOM_FILTERS } from "./features/rooms/constants";
import { useRooms } from "./features/rooms/hooks/useRooms";
import { filterRooms } from "./features/rooms/utils/filterRooms";
import { loadCurrentUser, logout } from "./services/authApi";

export function App() {
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState(ROOM_FILTERS.ALL);
  const [editingRoom, setEditingRoom] = useState(null);
  const [viewingRoom, setViewingRoom] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthReady, setIsAuthReady] = useState(false);
  const [showUserManager, setShowUserManager] = useState(false);

  const { rooms, isLoading, error, reloadRooms, removeRoom } = useRooms(Boolean(currentUser));
  const filteredRooms = useMemo(() => filterRooms(rooms, query, filter), [rooms, query, filter]);
  const isAdmin = currentUser?.role === "ADMIN";

  useEffect(() => {
    loadCurrentUser()
      .then(setCurrentUser)
      .catch(() => setCurrentUser(null))
      .finally(() => setIsAuthReady(true));
  }, []);

  async function handleLogout() {
    await logout();
    setCurrentUser(null);
    setEditingRoom(null);
    setShowUserManager(false);
  }

  async function handleDeleteRoom(room) {
    if (!isAdmin) return;
    const confirmed = window.confirm(`Xoa phong ${room.code}?`);
    if (!confirmed) return;

    await removeRoom(room.id);
    setViewingRoom(null);
  }

  if (!isAuthReady) {
    return (
      <main className="grid min-h-screen place-items-center bg-slate-100 px-4">
        <div className="grid place-items-center gap-3 text-slate-700">
          <div className="grid size-14 place-items-center rounded-lg bg-teal-700 text-3xl font-black text-white">M</div>
          <strong className="text-2xl font-black">MANHGG</strong>
        </div>
      </main>
    );
  }

  if (!currentUser) {
    return <LoginPage onLoggedIn={setCurrentUser} />;
  }

  return (
    <>
      <AppHeader
        user={currentUser}
        isAdmin={isAdmin}
        onCreateRoom={() => setEditingRoom(EMPTY_ROOM)}
        onLogout={handleLogout}
        onManageUsers={() => setShowUserManager(true)}
      />

      <main className="mx-auto my-6 w-[min(1180px,calc(100%-32px))]">
        <RoomToolbar query={query} filter={filter} onQueryChange={setQuery} onFilterChange={setFilter} />

        {error && (
          <p className="mb-5 rounded-lg border border-red-300 bg-red-50 p-6 text-center font-bold text-red-600">
            {error}
          </p>
        )}

        {isLoading && <EmptyState message="Dang tai danh sach phong..." />}

        {!isLoading && filteredRooms.length === 0 && (
          <EmptyState title="Chua co phong nao" message='Bam "Them phong" de luu phong dau tien.' />
        )}

        <RoomGrid rooms={filteredRooms} onOpenRoom={setViewingRoom} />
      </main>

      {isAdmin && editingRoom && (
        <RoomForm
          room={editingRoom}
          onClose={() => setEditingRoom(null)}
          onSaved={async () => {
            setEditingRoom(null);
            await reloadRooms();
          }}
        />
      )}

      {viewingRoom && (
        <RoomDetail
          room={rooms.find((room) => room.id === viewingRoom.id) || viewingRoom}
          canManage={isAdmin}
          onClose={() => setViewingRoom(null)}
          onEdit={(room) => {
            setViewingRoom(null);
            setEditingRoom(room);
          }}
          onDelete={handleDeleteRoom}
        />
      )}

      {isAdmin && showUserManager && <UserManager onClose={() => setShowUserManager(false)} />}
    </>
  );
}
