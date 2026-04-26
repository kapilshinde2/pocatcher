package com.boltech.pokemon.pocatcher.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonListPageDTO {
    private List<PokemonListItemDTO> items;
    private int totalCount;
    private int offset;
    private int limit;
    private boolean hasNext;
}
