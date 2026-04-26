package com.boltech.pokemon.pocatcher.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
public class PokemonListResponseDTO implements Serializable {
    private int count;
    private String next;
    private String previous;
    private List<PokemonListItemResponseDTO> results;
}
