export const ROOM_FILTERS = {
  ALL: "all",
  OPEN: "open",
  LOCK: "lock",
};

export const ROOM_STATUSES = [ROOM_FILTERS.OPEN, ROOM_FILTERS.LOCK];

export const EMPTY_ROOM = {
  id: "",
  address: "",
  price: "",
  code: "",
  status: ROOM_FILTERS.OPEN,
  note: "",
};
