package com.trader.journal_backend.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trader.journal_backend.dto.ExchangeSupportResponse;
import com.trader.journal_backend.exchange.ExchangeFactory;
import com.trader.journal_backend.model.ExchangeSymbol;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import com.trader.journal_backend.repository.ExchangeSymbolRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeFactory exchangeFactory;
    private final ExchangeSymbolRepository symbolRepository;

    @GetMapping("/supported")
    public ResponseEntity<List<ExchangeSupportResponse>> getSupportedMetadata() {
        List<ExchangeSupportResponse> metadata = Arrays.stream(ExchangePlatform.values())
                .filter(platform -> {
                    try {
                        return exchangeFactory.getProvider(platform).isSupported();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(platform -> new ExchangeSupportResponse(
                        platform.getId(),
                        platform.name(),
                        platform.getDisplayName(),
                        exchangeFactory.getProvider(platform).getSupportedMarketTypes()))
                .toList();

        return ResponseEntity.ok(metadata);
    }

    @GetMapping
    public ResponseEntity<List<ExchangePlatform>> getSupportedExchanges() {
        List<ExchangePlatform> supported = Arrays.stream(ExchangePlatform.values())
                .filter(platform -> {
                    try {
                        return exchangeFactory.getProvider(platform).isSupported();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
        return ResponseEntity.ok(supported);
    }

    @GetMapping("/{platform}/markets")
    public ResponseEntity<List<MarketType>> getMarkets(@PathVariable ExchangePlatform platform) {
        return ResponseEntity.ok(exchangeFactory.getProvider(platform).getSupportedMarketTypes());
    }

    @GetMapping("/{platform}/markets/{marketType}/symbols")
    public ResponseEntity<List<ExchangeSymbol>> getSymbols(
            @PathVariable ExchangePlatform platform,
            @PathVariable MarketType marketType) {

        if (!exchangeFactory.getProvider(platform).getSupportedMarketTypes().contains(marketType)) {
            throw new RuntimeException("Market " + marketType + " is not supported on " + platform);
        }
        List<ExchangeSymbol> symbols = symbolRepository.findByExchangeAndMarketType(platform, marketType);
        return ResponseEntity.ok(symbols);
    }
}