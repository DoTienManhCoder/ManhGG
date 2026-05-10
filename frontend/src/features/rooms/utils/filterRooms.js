import { ROOM_FILTERS } from "../constants";
import { normalizeText } from "../../../lib/normalizeText";

export function filterRooms(rooms, query, filter) {
  const keyword = normalizeText(query);

  return rooms.filter((room) => {
    const matchesStatus = filter === ROOM_FILTERS.ALL || room.status === filter;
    const searchableText = normalizeText(`${room.address} ${room.realAddress} ${room.price} ${room.code} ${room.note}`);
    return matchesStatus && searchableText.includes(keyword);
  });
}
