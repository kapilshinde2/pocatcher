import { getTypeColor } from "@/lib/utils";
import type { PokemonDetails } from "@/types/pokemon";

interface PokemonDetailsPopupProps {
    pokemon: PokemonDetails | undefined;
    isLoading: boolean;
    error: Error | null;
}

const PokemonDetailsPopup = ({ pokemon, isLoading, error }: PokemonDetailsPopupProps) => {
    if (isLoading) return <div>Loading...</div>
    if (error) return <div>Error: {error.message}</div>
    if (!pokemon) return <div>Pokemon not found</div>

    return (
        <div className="flex flex-col items-center justify-center">

            <h2 className="text-xl font-bold capitalize text-gray-800">
                {pokemon.name}
            </h2>

            <div className="flex items-center justify-center bg-slate-50 p-6">
                <img
                    src={pokemon.frontImage}
                    alt={pokemon.name}
                    className="w-32 h-32 object-contain transition-transform duration-300 group-hover:scale-110"
                    loading="lazy"
                />
                <img
                    src={pokemon.backImage}
                    alt={pokemon.name}
                    className="w-32 h-32 object-contain transition-transform duration-300 group-hover:scale-110"
                    loading="lazy"
                />
            </div>

            <div className="p-4 flex flex-col items-center gap-2">
                <span className="text-sm font-mono text-gray-400">#{pokemon.id.toString().padStart(3, '0')}</span>

                <h3 className="text-lg font-bold capitalize text-gray-800">Types</h3>
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

                <div className="flex gap-2 mt-1">
                    <span className="text-sm font-mono text-gray-500">Region: {pokemon.region}</span>
                </div>

                <h3 className="text-lg font-bold capitalize text-gray-800">Weaknesses</h3>

                <div className="flex flex-wrap gap-4 mt-4 justify-center">
                    {Object.entries(pokemon.weaknesses).map(([type, dangerFromType]) => (
                        <div key={type} className="flex flex-col items-center gap-2">
                            <span
                                className={`px-4 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest text-white shadow-sm ${getTypeColor(type)}`}
                            >
                                {type}
                            </span>

                            <div className="flex flex-wrap justify-center gap-1.5">
                                {dangerFromType.map((t) => (
                                    <span
                                        key={t}
                                        className={`px-2 py-0.5 rounded-md text-[10px] font-medium uppercase text-white opacity-90 ${getTypeColor(t)}`}
                                    >
                                        {t}
                                    </span>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

            </div>
        </div>
    )
}

export default PokemonDetailsPopup;
