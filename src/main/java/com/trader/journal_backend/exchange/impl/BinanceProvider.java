package com.trader.journal_backend.exchange.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.exchange.AbstractExchangeProvider;

@Service
public class BinanceProvider extends AbstractExchangeProvider {

    @Override
    public String getExchangeName() { return "BINANCE"; }

    @Override
    public List<OrderDTO> fetchTradeHistory(String apiKey, String secretKey, String symbol, Long startTime) {
        logEvent("Fetching history for " + symbol);
        
        return List.of(); 
    }

    @Override
    public void startRealtimeListener(String apiKey, String secretKey, String symbol) {
    }
}