import { useState } from "react"
import { usePokemonInfiniteList } from "../hooks/usePokemonInfiniteList"
import { usePokemonDetails } from "../hooks/usePokemonDetails"
import { List } from "react-window"
import PokemonRowComponent from "./PokemonRowComponent"
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog"
import PokemonDetailsPopup from "./PokemonDetailsPopup"

const ROW_HEIGHT_PX = 420
const LOAD_MORE_THRESHOLD_ROWS = 5

export default function Home() {
  const {
    pokemons,
    isPending,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = usePokemonInfiniteList(20)

  const [selectedPokemonId, setSelectedPokemonId] = useState<number | null>(null)

  const {
    data: pokemonDetails,
    isLoading: isDetailsLoading,
    error: detailsError,
  } = usePokemonDetails(selectedPokemonId)

  const handleRowsRendered = (visible: {
    startIndex: number
    stopIndex: number
  }) => {
    if (pokemons.length === 0) return
    if (!hasNextPage || isFetchingNextPage) return
    const triggerIndex = Math.max(0, pokemons.length - LOAD_MORE_THRESHOLD_ROWS)
    if (visible.stopIndex >= triggerIndex) {
      void fetchNextPage()
    }
  }

  if (isPending) return <div>Loading...</div>
  if (isError) return <div>Error: {error.message}</div>

  return (
    <>
      <div className="flex flex-col items-center justify-center gap-2">
        <List
          rowComponent={PokemonRowComponent}
          rowCount={pokemons.length}
          rowHeight={ROW_HEIGHT_PX}
          rowProps={{ pokemons, onSelect: setSelectedPokemonId }}
          onRowsRendered={handleRowsRendered}
          style={{ height: "100vh", width: "100vw" }}
        />
        {isFetchingNextPage ? (
          <div className="text-sm text-muted-foreground">Loading more…</div>
        ) : null}
        {!hasNextPage && pokemons.length > 0 ? (
          <div className="text-sm text-muted-foreground">End of list</div>
        ) : null}
      </div>

      {/* Single Dialog instance outside the virtualized list — avoids remounts on scroll */}
      <Dialog
        open={selectedPokemonId !== null}
        onOpenChange={(open) => { if (!open) setSelectedPokemonId(null) }}
      >
        <DialogContent showCloseButton={false} className="w-2xl max-w-2xl shadow-xl">
          <DialogTitle className="text-center">Pokemon Details</DialogTitle>
          {selectedPokemonId !== null && (
            <PokemonDetailsPopup
              pokemon={pokemonDetails}
              isLoading={isDetailsLoading}
              error={detailsError}
            />
          )}
        </DialogContent>
      </Dialog>
    </>
  )
}