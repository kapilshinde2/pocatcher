package com.boltech.pokemon.pocatcher.service;

import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cache façade for the Pokémon catalog and details.
 *
 * <p>Both caches are populated in a single warm-up pass via {@link #warmUp()}.
 * The list cache stores all Pokémon as a flat list keyed by {@code 'all'}.
 * The details cache stores individual Pokémon keyed by their numeric ID.
 *
 * <p>On a cache miss (cold start or Redis unavailable), {@link #getAllPokemonCatalog()}
 * falls through to {@link PokemonCatalogFetcher} which fetches from PokeAPI directly.
 *
 * <p>Self-injection via {@code @Lazy} is used so that {@code warmUp()} can call
 * {@code putDetailsCache()} through the Spring proxy, ensuring {@code @CachePut}
 * is intercepted correctly.
 */
@Service
public class PokemonCatalogCacheService {

    public static final String CACHE_NAME = "pokemonListCatalog";

    private final PokemonCatalogFetcher fetcher;

    // Self-reference through the proxy so @CachePut on putDetailsCache is intercepted.
    @Autowired
    @Lazy
    private PokemonCatalogCacheService self;

    public PokemonCatalogCacheService(PokemonCatalogFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Returns the full Pokémon list from cache.
     * On a cache miss, triggers a lightweight PokeAPI fetch — exactly one call per
     * Pokémon — via {@link PokemonCatalogFetcher#fetchAllListItems()}. The details
     * cache is intentionally not populated here; warm-up via {@link #warmUp()} is
     * the only path that performs the expensive enrichment fan-out.
     */
    @Cacheable(cacheNames = CACHE_NAME, key = "'all'")
    public List<PokemonListItemDTO> getAllPokemonCatalog() {
        return fetcher.fetchAllListItems();
    }

    /**
     * Executes a single enriched fetch pass and populates both the list cache
     * and the details cache ({@value PocatcherDetailsService#CACHE_NAME}) in one go.
     * Called by {@link PokeAPIDataLoader} at startup and on the daily refresh schedule.
     *
     * @return the list of all Pokémon list items (also stored in the list cache)
     */
    @CachePut(cacheNames = CACHE_NAME, key = "'all'")
    public List<PokemonListItemDTO> warmUp() {
        List<PokemonCatalogFetcher.PokemonFetchResult> results = fetcher.fetchAll();

        // Route through the proxy so @CachePut on putDetailsCache is intercepted by Spring AOP.
        results.forEach(r -> {
            if (r.details() != null && r.details().getId() != null) {
                self.putDetailsCache(r.details());
            }
        });

        return results.stream()
                .map(PokemonCatalogFetcher.PokemonFetchResult::listItem)
                .toList();
    }

    /**
     * Stores a single Pokémon's details into the details cache.
     * Called via {@code self} (the Spring proxy) from {@link #warmUp()} to ensure
     * the {@code @CachePut} annotation is intercepted correctly.
     */
    @CachePut(cacheNames = PocatcherDetailsService.CACHE_NAME, key = "#details.id")
    public PokemonDetailsDTO putDetailsCache(PokemonDetailsDTO details) {
        return details;
    }
}
