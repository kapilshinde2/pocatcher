package com.boltech.pokemon.pocatcher.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private final PokeAPIConfig config;

    public RestClientConfig(PokeAPIConfig config) {
        this.config = config;
    }

    @Bean
    RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getHttpclientConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(config.getHttpclientReadTimeout());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
