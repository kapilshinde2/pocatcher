package com.boltech.pokemon.pocatcher;

import com.boltech.pokemon.pocatcher.config.PokeAPIConfig;
import com.boltech.pokemon.pocatcher.dtos.PokemonListItemDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListItemResponseDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListResponseDTO;
import com.boltech.pokemon.pocatcher.exception.CatalogFetchInterruptedException;
import com.boltech.pokemon.pocatcher.service.PokemonCatalogFetcher;
import com.boltech.pokemon.pocatcher.service.PokemonCatalogFetcher.PokemonFetchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonCatalogFetcherTest {

    @Mock
    private PokeAPIConfig config;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private PokemonCatalogFetcher fetcher;

    @BeforeEach
    void setUp() {
        when(config.getBaseurl()).thenReturn("https://pokeapi.co/api/v2");
        when(config.getCatalogFetchConcurrency()).thenReturn(20);
        fetcher = new PokemonCatalogFetcher(config, restClient);
    }

    @Test
    void fetchAll_ShouldReturnList_WhenApiCallsSucceed() {
        PokemonListResponseDTO countResponse = new PokemonListResponseDTO();
        countResponse.setCount(1);

        PokemonListResponseDTO urlResponse = new PokemonListResponseDTO();
        PokemonListItemResponseDTO pokemonListItemResponseDTO = new PokemonListItemResponseDTO();
        pokemonListItemResponseDTO.setName("Test");
        String detailUrl = "https://pokeapi.co/api/v2/pokemon/1/";
        pokemonListItemResponseDTO.setUrl(detailUrl);
        urlResponse.setResults(List.of(pokemonListItemResponseDTO));

        PokemonListItemDTO detailDto = new PokemonListItemDTO();
        detailDto.setName("bulbasaur");

        when(restClient.get().uri(contains("limit=1")).retrieve().body(PokemonListResponseDTO.class))
                .thenReturn(countResponse);

        when(restClient.get().uri(contains("offset=0")).retrieve().body(PokemonListResponseDTO.class))
                .thenReturn(urlResponse);

        when(restClient.get().uri(detailUrl).retrieve().body(com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO.class))
                .thenReturn(null);

        List<PokemonFetchResult> result = fetcher.fetchAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).listItem().getName()).isNull(); // null details produces empty listItem
    }

    @Test
    void fetchAll_ShouldThrowCatalogFetchInterruptedException_WhenThreadIsInterrupted() {
        // Arrange: set up the count response so the fetcher gets past getTotalPokemonCount()
        PokemonListResponseDTO countResponse = new PokemonListResponseDTO();
        countResponse.setCount(1); // non-zero so it tries to fetch URLs via invokeAll
        when(restClient.get().uri(contains("limit=1")).retrieve().body(PokemonListResponseDTO.class))
                .thenReturn(countResponse);

        // Interrupt the current thread before calling fetchAll().
        // When executor.invokeAll() is called on an already-interrupted thread it throws
        // InterruptedException immediately, which the fetcher catches and wraps.
        Thread.currentThread().interrupt();

        try {
            assertThatThrownBy(() -> fetcher.fetchAll())
                    .isInstanceOf(CatalogFetchInterruptedException.class);

            // The fetcher must re-set the interrupt flag via Thread.currentThread().interrupt()
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            // Clear the interrupt flag so it doesn't bleed into subsequent tests
            Thread.interrupted();
        }
    }
}
