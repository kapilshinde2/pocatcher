package com.boltech.pokemon.pocatcher.dtos;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonDamageRelationsContainerDTO implements Serializable{
    private static final long serialVersionUID = 1L;
    @JsonProperty("damage_relations")
    private PokemonDamageRelationsDTO damageRelations;
}
