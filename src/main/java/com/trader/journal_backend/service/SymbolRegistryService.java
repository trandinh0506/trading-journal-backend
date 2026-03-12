package com.trader.journal_backend.service;

import com.trader.journal_backend.exchange.ExchangeFactory;
import com.trader.journal_backend.exchange.ExchangeProvider;
import com.trader.journal_backend.model.ExchangeSymbol;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import com.trader.journal_backend.repository.ExchangeSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SymbolRegistryService {

    private final ExchangeSymbolRepository symbolRepository;
    private final ExchangeFactory exchangeFactory;

    @Async
    @Transactional
    public void refreshAllSymbols(boolean isStartup) {
        if (isStartup && symbolRepository.count() > 0) {
            log.info("DB has data for symbols skipping auto update symbols at start up.");
            return; 
        }

        log.info("Starting update symbols from exchanges...");

        for (ExchangePlatform platform : ExchangePlatform.values()) {
            try {
                ExchangeProvider provider = exchangeFactory.getProvider(platform);
                
                for (MarketType marketType : List.of(MarketType.SPOT, MarketType.FUTURES)) {
                    syncSymbolsForPlatform(provider, marketType);
                }
            } catch (Exception e) {
                log.error("Can not update symbol for exchange {}: {}", platform, e.getMessage());
            }
        }
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledRefresh() {
        log.info("Auto update symbols for new day...");
        this.refreshAllSymbols(false);
    }

    private void syncSymbolsForPlatform(ExchangeProvider provider, MarketType marketType) {
        log.info("Getting symbols for {} - {}", provider.getExchange(), marketType);
        
        List<ExchangeSymbol> remoteSymbols = provider.fetchAvailableSymbols(marketType);
        if (remoteSymbols.isEmpty()) return;

        Set<String> existingCodes = symbolRepository.findByExchangeAndMarketType(provider.getExchange(), marketType)
                .stream()
                .map(ExchangeSymbol::getCode)
                .collect(Collectors.toSet());

        List<ExchangeSymbol> newSymbols = remoteSymbols.stream()
                .filter(s -> !existingCodes.contains(s.getCode()))
                .collect(Collectors.toList());

        if (!newSymbols.isEmpty()) {
            symbolRepository.saveAll(newSymbols);
            log.info("Added {} new symbols for {} - {}", newSymbols.size(), provider.getExchange(), marketType);
        } else {
            log.info("No any new symbol for {} - {}", provider.getExchange(), marketType);
        }
    }
}