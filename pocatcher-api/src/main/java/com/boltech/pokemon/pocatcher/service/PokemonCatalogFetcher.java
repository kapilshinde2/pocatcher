package com.boltech.pokemon.pocatcher.service;

import com.boltech.pokemon.pocatcher.config.PokeAPIConfig;
import com.boltech.pokemon.pocatcher.dtos.GenerationDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDamageRelationsContainerDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListItemDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListResponseDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonSpeciesDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonTypeEntryDTO;
import com.boltech.pokemon.pocatcher.exception.CatalogFetchInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * Fetches the full Pokémon catalog from PokeAPI in a single parallel pass.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Fetch total count with one request.</li>
 *   <li>Fan out paginated URL-list requests (no semaphore — low volume, ~13 pages).</li>
 *   <li>For each Pokémon URL, acquire a semaphore permit and make up to 4 sequential
 *       PokeAPI calls (base data, species, generation, type weaknesses). This produces
 *       both a {@link PokemonListItemDTO} and a fully-enriched {@link PokemonDetailsDTO}
 *       from a single pass, so both caches are populated without a second warm-up loop.</li>
 * </ol>
 *
 * <p>Concurrency is capped by {@code pokeapi.catalog-fetch-concurrency} (default 20).
 * At 20 concurrent slots × ~3.5 calls × ~300 ms latency ≈ 67 PokeAPI req/s,
 * safely under the ~100 req/s threshold.
 */
@Component
public class PokemonCatalogFetcher {

    private static final Logger log = LoggerFactory.getLogger(PokemonCatalogFetcher.class);
    private static final int PAGE_LIMIT = 100;
    private static final String UNKNOWN_REGION = "Unknown";

    private final PokeAPIConfig config;
    private final org.springframework.web.client.RestClient restClient;
    private final Semaphore semaphore;

    public PokemonCatalogFetcher(PokeAPIConfig config, org.springframework.web.client.RestClient restClient) {
        this.config = config;
        this.restClient = restClient;
        this.semaphore = new Semaphore(config.getCatalogFetchConcurrency());
    }

    /**
     * Paired result of one Pokémon fetch: the lightweight list item and the full details object.
     */
    public record PokemonFetchResult(PokemonListItemDTO listItem, PokemonDetailsDTO details) {}

    /**
     * Fetches and enriches all Pokémon in one parallel pass.
     * Returns paired list-item + details for every Pokémon so both caches can be
     * populated from a single call without a second round-trip to PokeAPI.
     *
     * <p>Use this for warm-up only. For the cache-miss fallback (where details
     * are not retained), use {@link #fetchAllListItems()} which makes ~1 PokeAPI
     * call per Pokémon instead of ~3-4.
     */
    public List<PokemonFetchResult> fetchAll() {
        try {
            int totalCount = getTotalPokemonCount();
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<String> allUrls = fetchAllPokemonUrls(executor, totalCount);
                return fetchAndEnrichAll(executor, allUrls);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatalogFetchInterruptedException("Interrupted while synchronizing Pokémon catalog", e);
        }
    }

    /**
     * Fetches only the lightweight list-item view for every Pokémon.
     * Performs exactly one PokeAPI call per Pokémon (the base resource), with no
     * species, generation, or type-weakness lookups.
     *
     * <p>This is the cache-miss fallback path for {@code getAllPokemonCatalog()}:
     * since the caller only needs list fields and does not populate the details
     * cache, the extra enrichment work performed by {@link #fetchAll()} would be
     * pure waste (~5200 unnecessary PokeAPI calls for ~1300 Pokémon).
     */
    public List<PokemonListItemDTO> fetchAllListItems() {
        try {
            int totalCount = getTotalPokemonCount();
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<String> allUrls = fetchAllPokemonUrls(executor, totalCount);
                return fetchListItemsOnly(executor, allUrls);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatalogFetchInterruptedException("Interrupted while synchronizing Pokémon catalog", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private int getTotalPokemonCount() {
        PokemonListResponseDTO response = restClient.get()
                .uri(config.getBaseurl() + "/pokemon?limit=1")
                .retrieve()
                .body(PokemonListResponseDTO.class);
        return (response != null) ? response.getCount() : 0;
    }

    private List<String> fetchAllPokemonUrls(ExecutorService executor, int totalCount) throws InterruptedException {
        List<Callable<List<String>>> tasks = new ArrayList<>();
        for (int offset = 0; offset < totalCount; offset += PAGE_LIMIT) {
            final int finalOffset = offset;
            tasks.add(() -> {
                PokemonListResponseDTO res = restClient.get()
                        .uri(config.getBaseurl() + "/pokemon?offset=" + finalOffset + "&limit=" + PAGE_LIMIT)
                        .retrieve()
                        .body(PokemonListResponseDTO.class);
                return res.getResults().stream().map(r -> r.getUrl()).toList();
            });
        }
        return executor.invokeAll(tasks).stream()
                .flatMap(f -> {
                    try {
                        return f.get().stream();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch Pokémon URL page", e);
                    }
                })
                .toList();
    }

    /**
     * For each Pokémon URL, acquires a semaphore permit and performs all enrichment
     * calls sequentially within that permit.
     */
    private List<PokemonFetchResult> fetchAndEnrichAll(ExecutorService executor, List<String> urls)
            throws InterruptedException {
        List<Callable<PokemonFetchResult>> tasks = urls.stream()
                .map(url -> (Callable<PokemonFetchResult>) () -> {
                    semaphore.acquire();
                    try {
                        return enrichPokemon(url);
                    } finally {
                        semaphore.release();
                    }
                })
                .toList();

        return executor.invokeAll(tasks).stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to enrich Pokémon", e);
                    }
                })
                .toList();
    }

    /**
     * Lightweight per-Pokémon fan-out: one PokeAPI call per URL, deserialized
     * directly into {@link PokemonListItemDTO}. Shares the same semaphore as the
     * enrichment path so concurrent connections stay bounded.
     */
    private List<PokemonListItemDTO> fetchListItemsOnly(ExecutorService executor, List<String> urls)
            throws InterruptedException {
        List<Callable<PokemonListItemDTO>> tasks = urls.stream()
                .map(url -> (Callable<PokemonListItemDTO>) () -> {
                    semaphore.acquire();
                    try {
                        return restClient.get().uri(url).retrieve().body(PokemonListItemDTO.class);
                    } finally {
                        semaphore.release();
                    }
                })
                .toList();

        return executor.invokeAll(tasks).stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch Pokémon list item", e);
                    }
                })
                .toList();
    }

    /**
     * Fetches base data, species/region, and type weaknesses for one Pokémon.
     * All calls are sequential and share the same semaphore permit, so the
     * concurrency cap applies to the full enrichment unit, not individual calls.
     */
    private PokemonFetchResult enrichPokemon(String pokemonUrl) {
        // 1. Base data — shared between list item and details
        PokemonDetailsDTO details = restClient.get()
                .uri(pokemonUrl)
                .retrieve()
                .body(PokemonDetailsDTO.class);

        if (details == null) {
            log.warn("Received null response for Pokémon URL: {}", pokemonUrl);
            return new PokemonFetchResult(new PokemonListItemDTO(), new PokemonDetailsDTO());
        }

        // 2. Region — from species → generation chain
        details.setRegion(fetchRegion(details.getId()));

        // 3. Weaknesses — from type endpoint(s)
        details.setWeaknesses(fetchWeaknesses(details));

        // 4. Project list item from the same enriched object (no extra PokeAPI call)
        PokemonListItemDTO listItem = new PokemonListItemDTO();
        listItem.setId(details.getId());
        listItem.setName(details.getName());
        listItem.setTypes(details.getTypes());
        listItem.setFrontImage(details.getFrontImage());

        return new PokemonFetchResult(listItem, details);
    }

    private String fetchRegion(Integer id) {
        if (id == null) return UNKNOWN_REGION;
        try {
            PokemonSpeciesDTO species = restClient.get()
                    .uri(config.getBaseurl() + "/pokemon-species/" + id)
                    .retrieve()
                    .body(PokemonSpeciesDTO.class);

            if (species == null || species.getGeneration() == null) return UNKNOWN_REGION;

            String generationUrl = species.getGeneration().getUrl();
            if (generationUrl == null || generationUrl.isBlank()) return UNKNOWN_REGION;

            GenerationDTO generation = restClient.get()
                    .uri(generationUrl)
                    .retrieve()
                    .body(GenerationDTO.class);

            return Optional.ofNullable(generation)
                    .map(GenerationDTO::getMainRegion)
                    .map(PokemonTypeDetailsDTO::getName)
                    .filter(name -> !name.isBlank())
                    .orElse(UNKNOWN_REGION);

        } catch (RestClientException e) {
            log.warn("Failed to fetch region for Pokémon id {}: {}", id, e.getMessage());
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
                    .uri(config.getBaseurl() + "/type/" + typeName)
                    .retrieve()
                    .body(PokemonDamageRelationsContainerDTO.class);

            return Optional.ofNullable(response)
                    .map(PokemonDamageRelationsContainerDTO::getDamageRelations)
                    .map(relations -> relations.getDoubleDamageFrom().stream()
                            .map(PokemonTypeDetailsDTO::getName)
                            .toList())
                    .orElse(Collections.emptyList());

        } catch (RestClientException e) {
            log.warn("Failed to fetch weaknesses for type {}: {}", typeName, e.getMessage());
            return Collections.emptyList();
        }
    }
}
