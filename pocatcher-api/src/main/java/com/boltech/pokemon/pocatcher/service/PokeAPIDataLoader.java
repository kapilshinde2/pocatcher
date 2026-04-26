package com.boltech.pokemon.pocatcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// DataLoader implementation that loads data from PokeAPI
@Service
public class PokeAPIDataLoader implements DataLoader {

    private static final Logger log = LoggerFactory.getLogger(PokeAPIDataLoader.class);

    private final PokemonCatalogCacheService catalogCacheService;

    PokeAPIDataLoader(PokemonCatalogCacheService catalogCacheService) {
        this.catalogCacheService = catalogCacheService;
    }

    @Override
    public void loadData() {
        log.info("Starting Pokémon catalog and details cache warm-up...");
        var items = catalogCacheService.warmUp();
        log.info("Cache warm-up complete — {} Pokémon loaded into list and details caches.", items.size());
    }
}
