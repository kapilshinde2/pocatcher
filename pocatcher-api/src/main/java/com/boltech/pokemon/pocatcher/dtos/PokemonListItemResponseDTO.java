package com.boltech.pokemon.pocatcher.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
public class PokemonListItemResponseDTO implements Serializable {
    private String name;
    private String url;
}
