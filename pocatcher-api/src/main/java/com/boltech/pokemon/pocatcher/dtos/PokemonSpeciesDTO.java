package com.boltech.pokemon.pocatcher.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonSpeciesDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Named API resource: {@code name} (e.g. generation-ii) and {@code url} to the generation resource. */
    private PokemonTypeDetailsDTO generation;
}
