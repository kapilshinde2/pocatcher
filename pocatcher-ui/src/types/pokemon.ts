export interface PokemonTypeDetails {
  name: string
  url: string
}

export interface PokemonTypeSlot {
  slot: number
  type: PokemonTypeDetails
}

export interface Pokemon {
  id: number
  name: string
  frontImage: string
  types: PokemonTypeSlot[]
}

export interface PokemonListPage {
  items: Pokemon[]
  totalCount: number
  offset: number
  limit: number
  hasNext: boolean
}

export interface PokemonDetails {
  backImage: string
  frontImage: string
  id: number
  name: string
  region: string
  types: PokemonTypeSlot[]
  weaknesses: Record<string, string[]>
}
