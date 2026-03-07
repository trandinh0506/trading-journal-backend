package com.trader.journal_backend.exchange;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import com.trader.journal_backend.model.ExchangeSymbol;

import java.util.List;

public interface ExchangeProvider {

    ExchangePlatform getExchange();

    List<ExchangeSymbol> fetchAvailableSymbols(MarketType marketType);

    List<OrderDTO> fetchTradeHistory(String apiKey, String secretKey, String symbol, MarketType marketType, Long startTime);

    void startRealtimeListener(String apiKey, String secretKey, List<String> symbols, MarketType marketType);
}