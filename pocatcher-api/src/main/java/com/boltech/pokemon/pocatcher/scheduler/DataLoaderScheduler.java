package com.boltech.pokemon.pocatcher.scheduler;

import com.boltech.pokemon.pocatcher.service.DataLoader;
import com.boltech.pokemon.pocatcher.service.PocatcherDetailsService;
import com.boltech.pokemon.pocatcher.service.PokemonCatalogCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderScheduler.class);

    private final DataLoader loader;

    public DataLoaderScheduler(DataLoader loader) {
        this.loader = loader;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStartup() {
        log.info("Warming pokemon catalog cache on application ready");
        loader.loadData();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @CacheEvict(cacheNames = {
            PokemonCatalogCacheService.CACHE_NAME,
            PocatcherDetailsService.CACHE_NAME
    }, allEntries = true, beforeInvocation = true)
    public void refreshCatalogDaily() {
        log.info("Refreshing pokemon catalog and details cache (scheduled)");
        loader.loadData();
    }
}
