import { Card } from "@/components/ui/card";
import type { Pokemon } from "@/types/pokemon"
import type { RowComponentProps } from "react-window";
import { getTypeColor } from "@/lib/utils";

function PokemonRowComponent({
  index,
  pokemons,
  style,
  ariaAttributes,
  onSelect,
}: RowComponentProps<{
  pokemons: Pokemon[]
  onSelect: (id: number) => void
}>) {
  const pokemon = pokemons[index]
  if (!pokemon) {
    return <div style={style} {...ariaAttributes} />
  }
  return (
    <div style={style} className="flex justify-center box-border px-2 py-1" {...ariaAttributes}>
      <Card
        onClick={() => onSelect(pokemon.id)}
        className="group w-auto min-w-md max-w-lg overflow-hidden transition-all duration-300 hover:shadow-xl hover:-translate-y-1 cursor-pointer border border-gray-200 rounded-xl bg-white"
      >
        <div className="flex items-center justify-center bg-slate-50 p-6">
          <img
            src={pokemon.frontImage}
            alt={pokemon.name}
            className="w-32 h-32 object-contain transition-transform duration-300 group-hover:scale-110"
            loading="lazy"
          />
        </div>

        <div className="p-4 flex flex-col items-center gap-2">
          <span className="text-sm font-mono text-gray-400">#{pokemon.id.toString().padStart(3, '0')}</span>

          <h2 className="text-xl font-bold capitalize text-gray-800">
            {pokemon.name}
          </h2>

          <div className="flex gap-2 mt-1">
            {pokemon.types.map((slot) => (
              <span
                key={slot.slot}
                className={`px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wider text-white ${getTypeColor(slot.type.name)}`}
              >
                {slot.type.name}
              </span>
            ))}
          </div>
        </div>
      </Card>
    </div>
  )
}

export default PokemonRowComponent