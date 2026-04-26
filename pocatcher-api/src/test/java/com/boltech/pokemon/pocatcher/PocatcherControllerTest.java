package com.boltech.pokemon.pocatcher;

import com.boltech.pokemon.pocatcher.controller.PocatcherController;
import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListPageDTO;
import com.boltech.pokemon.pocatcher.service.PocatcherDetailsService;
import com.boltech.pokemon.pocatcher.service.PocatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PocatcherControllerTest {

    @Mock
    private PocatcherService pocatcherService;

    @Mock
    private PocatcherDetailsService detailsService;

    private PocatcherController controller;

    @BeforeEach
    void setUp() {
        controller = new PocatcherController(pocatcherService, detailsService);
    }

    @Test
    void pokemonListCallsServiceWithSameLimitAndOffset() {
        PokemonListPageDTO page = new PokemonListPageDTO(List.of(), 3, 10, 15, false);
        when(pocatcherService.getPokemonPage(15, 10)).thenReturn(page);

        PokemonListPageDTO result = controller.getPokemonPage(15, 10);

        assertThat(result).isEqualTo(page);
        verify(pocatcherService).getPokemonPage(15, 10);
    }

    @Test
    void pokemonDetailsCallsServiceWithSameId() {
        PokemonDetailsDTO dto = new PokemonDetailsDTO();
        dto.setId(42);
        dto.setName("pikachu");
        when(detailsService.getPokemonDetails(42)).thenReturn(dto);

        PokemonDetailsDTO result = controller.getPokemonDetails(42);

        assertThat(result).isEqualTo(dto);
        verify(detailsService).getPokemonDetails(42);
    }
}
