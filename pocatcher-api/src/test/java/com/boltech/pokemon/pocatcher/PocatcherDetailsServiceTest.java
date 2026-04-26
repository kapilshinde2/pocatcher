package com.boltech.pokemon.pocatcher;

import com.boltech.pokemon.pocatcher.config.PokeAPIConfig;
import com.boltech.pokemon.pocatcher.dtos.GenerationDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDamageRelationsContainerDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDamageRelationsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonSpeciesDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeEntryDTO;
import com.boltech.pokemon.pocatcher.service.PocatcherDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PocatcherDetailsServiceTest {

    private static final String GENERATION_ONE_URL = "https://pokeapi.co/api/v2/generation/1/";

    @Mock
    private PokeAPIConfig config;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private PocatcherDetailsService detailsService;

    @BeforeEach
    void setUp() {
        when(config.getBaseurl()).thenReturn("https://pokeapi.co/api/v2");
        detailsService = new PocatcherDetailsService(config, restClient);
    }

    @Test
    void fillsRegionFromGenerationMainRegionAndWeaknessesFromType() {
        PokemonDetailsDTO pokemon = grassPokemon(1, "bulbasaur");
        PokemonSpeciesDTO species = speciesWithGenerationUrl(GENERATION_ONE_URL);
        GenerationDTO generation = generationWithMainRegion("kanto");
        PokemonDamageRelationsContainerDTO fireWeak = weaknessPayload("fire");

        when(restClient.get().uri(contains("/pokemon/1")).retrieve().body(PokemonDetailsDTO.class)).thenReturn(pokemon);
        when(restClient.get().uri(contains("/pokemon-species/1")).retrieve().body(PokemonSpeciesDTO.class)).thenReturn(species);
        when(restClient.get().uri(contains("/generation/1")).retrieve().body(GenerationDTO.class)).thenReturn(generation);
        when(restClient.get().uri(contains("/type/grass")).retrieve().body(PokemonDamageRelationsContainerDTO.class)).thenReturn(fireWeak);

        PokemonDetailsDTO result = detailsService.getPokemonDetails(1);

        assertThat(result.getRegion()).isEqualTo("kanto");
        assertThat(result.getWeaknesses().get("grass")).containsExactly("fire");
    }

    @Test
    void usesUnknownRegionWhenSpeciesLookupFails() {
        PokemonDetailsDTO pokemon = grassPokemon(99, "x");
        PokemonDamageRelationsContainerDTO emptyWeak = weaknessPayload();

        when(restClient.get().uri(contains("/pokemon/99")).retrieve().body(PokemonDetailsDTO.class)).thenReturn(pokemon);
        when(restClient.get().uri(contains("/pokemon-species/99")).retrieve().body(PokemonSpeciesDTO.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8));
        when(restClient.get().uri(contains("/type/grass")).retrieve().body(PokemonDamageRelationsContainerDTO.class)).thenReturn(emptyWeak);

        PokemonDetailsDTO result = detailsService.getPokemonDetails(99);

        assertThat(result.getRegion()).isEqualTo("Unknown");
    }

    @Test
    void usesUnknownRegionWhenGenerationLookupFails() {
        PokemonDetailsDTO pokemon = grassPokemon(2, "ivysaur");
        PokemonSpeciesDTO species = speciesWithGenerationUrl(GENERATION_ONE_URL);
        PokemonDamageRelationsContainerDTO emptyWeak = weaknessPayload();

        when(restClient.get().uri(contains("/pokemon/2")).retrieve().body(PokemonDetailsDTO.class)).thenReturn(pokemon);
        when(restClient.get().uri(contains("/pokemon-species/2")).retrieve().body(PokemonSpeciesDTO.class)).thenReturn(species);
        when(restClient.get().uri(contains("/generation/1")).retrieve().body(GenerationDTO.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8));
        when(restClient.get().uri(contains("/type/grass")).retrieve().body(PokemonDamageRelationsContainerDTO.class)).thenReturn(emptyWeak);

        PokemonDetailsDTO result = detailsService.getPokemonDetails(2);

        assertThat(result.getRegion()).isEqualTo("Unknown");
    }

    @Test
    void getPokemonDetails_withNullTypes_returnsEmptyWeaknesses() {
        PokemonDetailsDTO pokemon = new PokemonDetailsDTO();
        pokemon.setId(100);
        pokemon.setName("nulltype");
        pokemon.setTypes(null);

        when(restClient.get().uri(contains("/pokemon/100")).retrieve().body(PokemonDetailsDTO.class)).thenReturn(pokemon);
        when(restClient.get().uri(contains("/pokemon-species/100")).retrieve().body(PokemonSpeciesDTO.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8));

        PokemonDetailsDTO result = detailsService.getPokemonDetails(100);

        assertThat(result.getWeaknesses()).isEmpty();
    }

    @Test
    void getPokemonDetails_withEmptyTypes_returnsEmptyWeaknesses() {
        PokemonDetailsDTO pokemon = new PokemonDetailsDTO();
        pokemon.setId(101);
        pokemon.setName("emptytype");
        pokemon.setTypes(Collections.emptyList());

        when(restClient.get().uri(contains("/pokemon/101")).retrieve().body(PokemonDetailsDTO.class)).thenReturn(pokemon);
        when(restClient.get().uri(contains("/pokemon-species/101")).retrieve().body(PokemonSpeciesDTO.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8));

        PokemonDetailsDTO result = detailsService.getPokemonDetails(101);

        assertThat(result.getWeaknesses()).isEmpty();
    }

    private static PokemonDetailsDTO grassPokemon(int id, String name) {
        PokemonDetailsDTO dto = new PokemonDetailsDTO();
        dto.setId(id);
        dto.setName(name);
        PokemonTypeDetailsDTO grass = new PokemonTypeDetailsDTO();
        grass.setName("grass");
        PokemonTypeEntryDTO entry = new PokemonTypeEntryDTO();
        entry.setType(grass);
        dto.setTypes(List.of(entry));
        return dto;
    }

    private static PokemonSpeciesDTO speciesWithGenerationUrl(String generationUrl) {
        PokemonSpeciesDTO species = new PokemonSpeciesDTO();
        PokemonTypeDetailsDTO generation = new PokemonTypeDetailsDTO();
        generation.setName("generation-i");
        generation.setUrl(generationUrl);
        species.setGeneration(generation);
        return species;
    }

    private static GenerationDTO generationWithMainRegion(String regionName) {
        PokemonTypeDetailsDTO mainRegion = new PokemonTypeDetailsDTO();
        mainRegion.setName(regionName);
        GenerationDTO gen = new GenerationDTO();
        gen.setMainRegion(mainRegion);
        return gen;
    }

    private static PokemonDamageRelationsContainerDTO weaknessPayload(String... doubleDamageFromNames) {
        PokemonDamageRelationsDTO relations = new PokemonDamageRelationsDTO();
        relations.setDoubleDamageFrom(
                Arrays.stream(doubleDamageFromNames)
                        .map(n -> {
                            PokemonTypeDetailsDTO t = new PokemonTypeDetailsDTO();
                            t.setName(n);
                            return t;
                        })
                        .toList());
        PokemonDamageRelationsContainerDTO container = new PokemonDamageRelationsContainerDTO();
        container.setDamageRelations(relations);
        return container;
    }
}
