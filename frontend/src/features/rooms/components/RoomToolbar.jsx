import { Search } from "lucide-react";
import { fieldClass, labelClass } from "../../../components/ui/Field";
import { ROOM_FILTERS } from "../constants";

const filters = [ROOM_FILTERS.ALL, ROOM_FILTERS.OPEN, ROOM_FILTERS.LOCK];

export function RoomToolbar({ query, filter, onQueryChange, onFilterChange }) {
  return (
    <section className="mb-5 flex items-end justify-between gap-4 max-md:flex-col max-md:items-stretch">
      <label className="min-w-56 flex-1">
        <span className={labelClass}>Tìm phòng</span>
        <div className="relative">
          <Search className="absolute left-3 top-3 text-slate-500" size={18} />
          <input
            className={`${fieldClass} pl-10`}
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            type="search"
            placeholder="Địa chỉ, giá, mã phòng..."
          />
        </div>
      </label>

      <div className="grid h-10 w-full overflow-hidden rounded-lg border border-slate-600 bg-slate-800 md:w-90 md:grid-cols-3">
        {filters.map((item) => (
          <button
            className={`border-r border-slate-600 px-4 text-sm font-bold capitalize last:border-r-0 ${
              filter === item ? "bg-teal-500 text-slate-950" : "text-slate-300 hover:bg-slate-700"
            }`}
            key={item}
            type="button"
            onClick={() => onFilterChange(item)}
          >
            {item === ROOM_FILTERS.ALL ? "Tất cả" : item}
          </button>
        ))}
      </div>
    </section>
  );
}
