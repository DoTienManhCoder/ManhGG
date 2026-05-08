import { RoomCard } from "./RoomCard";

export function RoomGrid({ rooms, onOpenRoom }) {
  return (
    <section className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-4 max-sm:grid-cols-[repeat(auto-fill,minmax(170px,1fr))]">
      {rooms.map((room) => (
        <RoomCard key={room.id} room={room} onOpen={() => onOpenRoom(room)} />
      ))}
    </section>
  );
}
