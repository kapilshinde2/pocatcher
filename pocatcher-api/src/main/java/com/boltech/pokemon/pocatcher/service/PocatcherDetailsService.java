package com.boltech.pokemon.pocatcher.service;

import com.boltech.pokemon.pocatcher.config.PokeAPIConfig;
import com.boltech.pokemon.pocatcher.dtos.GenerationDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDamageRelationsContainerDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonSpeciesDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeEntryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Assembles full Pokémon details by orchestrating three PokeAPI calls:
 * the base Pokémon resource, the species resource (for region), and the
 * type resource (for weaknesses). Results are cached by Pokémon ID.
 *
 * <p>Under normal operation both caches are pre-warmed at startup by
 * {@link PokeAPIDataLoader}, so this service's PokeAPI calls only execute
 * on a cache miss (e.g. Redis restart or a Pokémon not yet in cache).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PocatcherDetailsService {

    public static final String CACHE_NAME = "pokemonDetails";

    private static final String UNKNOWN_REGION = "Unknown";
    private static final String POKEMON_PATH = "/pokemon/";
    private static final String POKEMON_SPECIES_PATH = "/pokemon-species/";
    private static final String TYPE_PATH = "/type/";

    private final PokeAPIConfig config;
    private final RestClient restClient;

    @Cacheable(cacheNames = CACHE_NAME, key = "#id")
    public PokemonDetailsDTO getPokemonDetails(Integer id) {
        PokemonDetailsDTO pokemon = fetchPokemonDetails(id);
        pokemon.setRegion(fetchRegion(id));
        pokemon.setWeaknesses(fetchWeaknesses(pokemon));
        return pokemon;
    }

    private PokemonDetailsDTO fetchPokemonDetails(Integer id) {
        return restClient.get()
                .uri(buildUrl(POKEMON_PATH + id))
                .retrieve()
                .body(PokemonDetailsDTO.class);
    }

    private String fetchRegion(Integer id) {
        try {
            PokemonSpeciesDTO species = restClient.get()
                    .uri(buildUrl(POKEMON_SPECIES_PATH + id))
                    .retrieve()
                    .body(PokemonSpeciesDTO.class);

            if (species == null || species.getGeneration() == null) {
                return UNKNOWN_REGION;
            }

            String generationUrl = species.getGeneration().getUrl();
            if (generationUrl == null || generationUrl.isBlank()) {
                return UNKNOWN_REGION;
            }

            try {
                GenerationDTO generation = restClient.get()
                        .uri(generationUrl)
                        .retrieve()
                        .body(GenerationDTO.class);

                return Optional.ofNullable(generation)
                        .map(GenerationDTO::getMainRegion)
                        .map(PokemonTypeDetailsDTO::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .orElse(UNKNOWN_REGION);
            } catch (RestClientException e) {
                log.warn("Failed to fetch generation for pokemon id {}: {}", id, e.getMessage());
                return UNKNOWN_REGION;
            }

        } catch (RestClientException e) {
            log.warn("Failed to fetch species details for pokemon id {}: {}", id, e.getMessage());
            return UNKNOWN_REGION;
        }
    }

    private Map<String, List<String>> fetchWeaknesses(PokemonDetailsDTO pokemon) {
        if (pokemon.getTypes() == null || pokemon.getTypes().isEmpty()) {
            return Collections.emptyMap();
        }

        return pokemon.getTypes().stream()
                .map(PokemonTypeEntryDTO::getType)
                .filter(type -> type != null && type.getName() != null)
                .collect(Collectors.toMap(
                        PokemonTypeDetailsDTO::getName,
                        type -> fetchWeaknessesByType(type.getName())
                ));
    }

    private List<String> fetchWeaknessesByType(String typeName) {
        try {
            PokemonDamageRelationsContainerDTO response = restClient.get()
                    .uri(buildUrl(TYPE_PATH + typeName))
                    .retrieve()
                    .body(PokemonDamageRelationsContainerDTO.class);

            return Optional.ofNullable(response)
                    .map(PokemonDamageRelationsContainerDTO::getDamageRelations)
                    .map(relations -> relations.getDoubleDamageFrom().stream()
                            .map(PokemonTypeDetailsDTO::getName)
                            .toList())
                    .orElse(Collections.emptyList());

        } catch (RestClientException e) {
            log.warn("Failed to fetch type details for type {}: {}", typeName, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildUrl(String path) {
        return config.getBaseurl() + path;
    }
}