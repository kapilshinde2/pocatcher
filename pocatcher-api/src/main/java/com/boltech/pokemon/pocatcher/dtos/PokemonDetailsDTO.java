package com.boltech.pokemon.pocatcher.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonDetailsDTO implements Serializable{
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private List<PokemonTypeEntryDTO> types;
    private String frontImage;
    private String backImage;

    /**
     * Jackson setter-based deserializer for the nested sprites JSON object.
     * Extracts both {@code front_default} and {@code back_default} directly from the
     * sprites map, avoiding the need for a full {@code SpritesDTO} wrapper class.
     *
     * @param sprites the raw sprites map deserialized from the PokeAPI response
     */
    @JsonProperty("sprites")
    private void unpackSprites(Map<String, Object> sprites) {
        this.frontImage = (String) sprites.get("front_default");
        this.backImage = (String) sprites.get("back_default");
    }

    private String region;
    private Map<String, List<String>> weaknesses;
}
