package com.boltech.pokemon.pocatcher.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GenerationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("main_region")
    private PokemonTypeDetailsDTO mainRegion;
}
