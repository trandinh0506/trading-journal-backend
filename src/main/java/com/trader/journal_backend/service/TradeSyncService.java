package com.trader.journal_backend.service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.exchange.ExchangeFactory;
import com.trader.journal_backend.exchange.ExchangeProvider;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.repository.TradeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeSyncService {

    private final ExchangeFactory exchangeFactory;
    private final TradeService tradeService;
    private final EncryptionService encryptionService;
    private final TradeRepository tradeRepository;

    public int syncAndProcess(UserExchangeConnection conn, String symbol) {
        long startTime = System.currentTimeMillis();
        ExchangeProvider provider = exchangeFactory.getProvider(conn.getPlatform());
        String rawSecret = encryptionService.decrypt(conn.getApiSecret());

        Long lastTimestamp = calculateStartTime(conn.getUserId(), symbol);

        log.info("SYNC_START | Platform: {} | Symbol: {} | From: {}", 
             conn.getPlatform(), symbol, lastTimestamp != null ? lastTimestamp : "BEGINNING");

        List<OrderDTO> remoteOrders = provider.fetchTradeHistory(
            conn.getApiKey(), 
            rawSecret, 
            symbol, 
            conn.getMarketType(), 
            lastTimestamp
        );

        log.info("SYNC_FETCHED | Received {} orders from {}", remoteOrders.size(), conn.getPlatform());

        int count = 0;
        for (OrderDTO dto : remoteOrders) {
            if (tradeService.processNewOrder(dto, conn.getUserId()) != null) {
                count++;
            }
        }
        log.info("SYNC_COMPLETE | Symbol: {} | Added: {} | Duration: {}ms", 
             symbol, count, System.currentTimeMillis() - startTime);
        return count;
    }
    private Long calculateStartTime(Long userId, String symbol) {
        // First, try to find the oldest OPEN trade for this symbol. We want to sync from that point to avoid missing any fills.
        String status = "OPEN";
        Optional<Trade> oldestOpenTrade = tradeRepository
                .findFirstByUserIdAndSymbolAndStatusOrderByOpenedAtAsc(userId, symbol, status);

        log.info("CALCULATE_START_TIME | User: {} | Symbol: {} | Oldest OPEN Trade: {}", 
             userId, symbol, oldestOpenTrade.map(Trade::getOpenedAt).orElse(null));

        
        if (oldestOpenTrade.isPresent()) {
            // Sync from 1 minute before the oldest OPEN trade to ensure we capture the opening fill and any related orders
            return oldestOpenTrade.get().getOpenedAt().minus(1, ChronoUnit.MINUTES).toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        // If there are no OPEN trades, take the last close trade as a reference point & sync from there
        Optional<Trade> latestClosedTrade = tradeRepository
                .findFirstByUserIdAndSymbolOrderByOpenedAtDesc(userId, symbol);
        
        return latestClosedTrade.map(trade -> 
                trade.getOpenedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .orElse(null); // Return null to let the Provider decide
    }
}