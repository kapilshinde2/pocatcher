package com.boltech.pokemon.pocatcher.config;

import java.time.Duration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "pokeapi")
public class PokeAPIConfig {

    @NotBlank
    private String baseurl;

    @NotNull
    private Duration httpclientConnectTimeout;

    @NotNull
    private Duration httpclientReadTimeout;

    @Min(1)
    @Max(50)
    private int catalogFetchConcurrency = 20;
}
