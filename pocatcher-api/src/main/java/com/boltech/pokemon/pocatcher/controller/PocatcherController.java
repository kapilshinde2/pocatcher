package com.boltech.pokemon.pocatcher.controller;

import com.boltech.pokemon.pocatcher.dtos.PokemonDetailsDTO;
import com.boltech.pokemon.pocatcher.dtos.PokemonListPageDTO;
import com.boltech.pokemon.pocatcher.service.PocatcherDetailsService;
import com.boltech.pokemon.pocatcher.service.PocatcherService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/pocatcher")
@Validated
public class PocatcherController {

    private final PocatcherService service;
    private final PocatcherDetailsService detailsService;

    public PocatcherController(PocatcherService service, PocatcherDetailsService detailsService) {
        this.service = service;
        this.detailsService = detailsService;
    }

    @GetMapping("/pokemons")
    public PokemonListPageDTO getPokemonPage(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return this.service.getPokemonPage(limit, offset);
    }

    @GetMapping("/pokemons/{id}")
    public PokemonDetailsDTO getPokemonDetails(@PathVariable int id) {
        return this.detailsService.getPokemonDetails(id);
    }
}
