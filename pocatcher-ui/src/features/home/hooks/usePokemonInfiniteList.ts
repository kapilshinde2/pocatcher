import { apiUrl } from "@/config/apiBase"
import type { PokemonListPage } from "@/types/pokemon"
import { useInfiniteQuery } from "@tanstack/react-query"
import { useMemo } from "react"

export async function fetchPokemonPage(limit: number, offset: number): Promise<PokemonListPage> {
  const params = new URLSearchParams({
    limit: String(limit),
    offset: String(offset),
  })
  const url = apiUrl(`/api/v1/pocatcher/pokemons?${params}`)
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`Failed to load pokemons (${res.status})`)
  }
  return res.json() as Promise<PokemonListPage>
}

export function usePokemonInfiniteList(pageSize = 20) {
  const query = useInfiniteQuery({
    queryKey: ["pokemons", "list", pageSize],
    initialPageParam: 0,
    queryFn: ({ pageParam }) => fetchPokemonPage(pageSize, pageParam as number),
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.offset + lastPage.limit : undefined,
  })

  const pokemons = useMemo(
    () => query.data?.pages.flatMap((p) => p.items) ?? [],
    [query.data],
  )

  return { ...query, pokemons }
}
