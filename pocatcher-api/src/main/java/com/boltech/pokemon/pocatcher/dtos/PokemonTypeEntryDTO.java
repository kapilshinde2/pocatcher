package com.boltech.pokemon.pocatcher.dtos;

import lombok.*;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PokemonTypeEntryDTO implements Serializable {
    private int slot;
    private PokemonTypeDetailsDTO type;
}
