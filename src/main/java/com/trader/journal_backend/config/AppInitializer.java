package com.trader.journal_backend.config;

import com.trader.journal_backend.service.SymbolRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppInitializer implements CommandLineRunner {

    private final SymbolRegistryService symbolRegistryService;

    @Override
    public void run(String... args) {
        symbolRegistryService.refreshAllSymbols(true);
    }
}