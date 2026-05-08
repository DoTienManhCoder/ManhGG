import { useCallback, useEffect, useState } from "react";
import { deleteRoom, fetchRooms } from "../../../services/roomsApi";

export function useRooms(enabled) {
  const [rooms, setRooms] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const reloadRooms = useCallback(async () => {
    if (!enabled) {
      setRooms([]);
      setIsLoading(false);
      setError("");
      return;
    }

    setIsLoading(true);
    setError("");
    try {
      setRooms(await fetchRooms());
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setIsLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    reloadRooms();
  }, [reloadRooms]);

  const removeRoom = useCallback(
    async (id) => {
      await deleteRoom(id);
      await reloadRooms();
    },
    [reloadRooms],
  );

  return { rooms, isLoading, error, reloadRooms, removeRoom };
}
