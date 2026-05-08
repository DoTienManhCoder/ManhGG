import { Search } from "lucide-react";
import { fieldClass, labelClass } from "../../../components/ui/Field";
import { ROOM_FILTERS } from "../constants";

const filters = [ROOM_FILTERS.ALL, ROOM_FILTERS.OPEN, ROOM_FILTERS.LOCK];

export function RoomToolbar({ query, filter, onQueryChange, onFilterChange }) {
  return (
    <section className="mb-5 flex items-end justify-between gap-4 max-md:flex-col max-md:items-stretch">
      <label className="min-w-56 flex-1">
        <span className={labelClass}>Tim phong</span>
        <div className="relative">
          <Search className="absolute left-3 top-3 text-slate-500" size={18} />
          <input
            className={`${fieldClass} pl-10`}
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            type="search"
            placeholder="Dia chi, gia, ma phong..."
          />
        </div>
      </label>

      <div className="grid h-10 w-full overflow-hidden rounded-lg border border-slate-200 bg-white md:w-90 md:grid-cols-3">
        {filters.map((item) => (
          <button
            className={`border-r border-slate-200 px-4 text-sm font-bold capitalize last:border-r-0 ${
              filter === item ? "bg-teal-700 text-white" : "text-slate-500 hover:bg-slate-50"
            }`}
            key={item}
            type="button"
            onClick={() => onFilterChange(item)}
          >
            {item === ROOM_FILTERS.ALL ? "Tat ca" : item}
          </button>
        ))}
      </div>
    </section>
  );
}
