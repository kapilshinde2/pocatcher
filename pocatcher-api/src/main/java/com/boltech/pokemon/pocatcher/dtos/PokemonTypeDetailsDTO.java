package com.boltech.pokemon.pocatcher.dtos;

import lombok.*;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PokemonTypeDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String url;
}
