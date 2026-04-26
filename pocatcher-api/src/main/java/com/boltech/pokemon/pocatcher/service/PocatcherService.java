package com.boltech.pokemon.pocatcher.service;

import com.boltech.pokemon.pocatcher.dtos.PokemonListItemDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListPageDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PocatcherService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;

    private final PokemonCatalogCacheService catalogCacheService;

    public PocatcherService(PokemonCatalogCacheService catalogCacheService) {
        this.catalogCacheService = catalogCacheService;
    }

    public PokemonListPageDTO getPokemonPage(int limit, int offset) {
        int safeLimit = clampLimit(limit);
        int safeOffset = Math.max(0, offset);

        List<PokemonListItemDTO> all = catalogCacheService.getAllPokemonCatalog();
        int totalCount = all.size();

        if (safeOffset >= totalCount) {
            return new PokemonListPageDTO(Collections.emptyList(), totalCount, safeOffset, safeLimit, false);
        }

        int toIndex = Math.min(safeOffset + safeLimit, totalCount);
        List<PokemonListItemDTO> slice = all.subList(safeOffset, toIndex);
        boolean hasNext = toIndex < totalCount;

        return new PokemonListPageDTO(slice, totalCount, safeOffset, safeLimit, hasNext);
    }

    private static int clampLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.clamp(limit, MIN_LIMIT, MAX_LIMIT);
    }
}
