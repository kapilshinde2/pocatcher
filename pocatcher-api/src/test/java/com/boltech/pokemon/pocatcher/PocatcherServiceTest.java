package com.boltech.pokemon.pocatcher;

import com.boltech.pokemon.pocatcher.dtos.PokemonListItemDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListPageDTO;
import com.boltech.pokemon.pocatcher.service.PocatcherService;
import com.boltech.pokemon.pocatcher.service.PokemonCatalogCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PocatcherServiceTest {

    @Mock
    private PokemonCatalogCacheService catalogCacheService;

    private PocatcherService pocatcherService;

    @BeforeEach
    void setUp() {
        pocatcherService = new PocatcherService(catalogCacheService);
    }

    @Test
    void getPokemonPage_firstPage() {
        List<PokemonListItemDTO> all = buildNamedList(25);
        when(catalogCacheService.getAllPokemonCatalog()).thenReturn(all);

        PokemonListPageDTO page = pocatcherService.getPokemonPage(10, 0);

        assertThat(page.getItems()).hasSize(10);
        assertThat(page.getTotalCount()).isEqualTo(25);
        assertThat(page.getOffset()).isZero();
        assertThat(page.getLimit()).isEqualTo(10);
        assertThat(page.isHasNext()).isTrue();
        assertThat(page.getItems().get(0).getName()).isEqualTo("pokemon-0");
    }

    @Test
    void getPokemonPage_lastPage_partial() {
        List<PokemonListItemDTO> all = buildNamedList(25);
        when(catalogCacheService.getAllPokemonCatalog()).thenReturn(all);

        PokemonListPageDTO page = pocatcherService.getPokemonPage(10, 20);

        assertThat(page.getItems()).hasSize(5);
        assertThat(page.isHasNext()).isFalse();
        assertThat(page.getOffset()).isEqualTo(20);
    }

    @Test
    void getPokemonPage_offsetBeyondEnd() {
        List<PokemonListItemDTO> all = buildNamedList(3);
        when(catalogCacheService.getAllPokemonCatalog()).thenReturn(all);

        PokemonListPageDTO page = pocatcherService.getPokemonPage(10, 10);

        assertThat(page.getItems()).isEmpty();
        assertThat(page.getTotalCount()).isEqualTo(3);
        assertThat(page.isHasNext()).isFalse();
    }

    @Test
    void getPokemonPage_emptyCatalog() {
        when(catalogCacheService.getAllPokemonCatalog()).thenReturn(new ArrayList<>());

        PokemonListPageDTO page = pocatcherService.getPokemonPage(20, 0);

        assertThat(page.getItems()).isEmpty();
        assertThat(page.getTotalCount()).isZero();
        assertThat(page.isHasNext()).isFalse();
    }

    private static List<PokemonListItemDTO> buildNamedList(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> {
                    PokemonListItemDTO dto = new PokemonListItemDTO();
                    dto.setName("pokemon-" + i);
                    return dto;
                })
                .toList();
    }
}
