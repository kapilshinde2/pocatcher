import { apiUrl } from "@/config/apiBase"
import type { PokemonDetails } from "@/types/pokemon"
import { useQuery } from "@tanstack/react-query"

export function usePokemonDetails(pokemonId: number | null) {
  return useQuery<PokemonDetails>({
    queryKey: ["pokemonDetails", pokemonId],
    queryFn: async () => {
      const res = await fetch(apiUrl(`/api/v1/pocatcher/pokemons/${pokemonId}`))
      if (!res.ok) throw new Error(`Failed to load Pokémon details (${res.status})`)
      return res.json() as Promise<PokemonDetails>
    },
    enabled: pokemonId !== null,
  })
}
