package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.ExchangeSymbol;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeSymbolRepository extends JpaRepository<ExchangeSymbol, Long> {

    List<ExchangeSymbol> findByExchange(ExchangePlatform exchange);

    List<ExchangeSymbol> findByExchangeAndMarketType(ExchangePlatform exchange, MarketType marketType);

    Optional<ExchangeSymbol> findByExchangeAndMarketTypeAndCode(
        ExchangePlatform exchange, MarketType marketType, String code);

    List<ExchangeSymbol> findByExchangeAndMarketTypeAndQuoteAsset(
        ExchangePlatform exchange, MarketType marketType, String quoteAsset);
}