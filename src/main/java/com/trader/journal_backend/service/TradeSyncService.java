package com.trader.journal_backend.service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.exchange.ExchangeFactory;
import com.trader.journal_backend.exchange.ExchangeProvider;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeSyncService {

    private final ExchangeFactory exchangeFactory;
    private final TradeService tradeService;
    private final EncryptionService encryptionService;
    private final OrderRepository orderRepository;

    public int syncAndProcess(UserExchangeConnection conn, String symbol) {
        long startTime = System.currentTimeMillis();
        ExchangeProvider provider = exchangeFactory.getProvider(conn.getPlatform());
        String rawSecret = encryptionService.decrypt(conn.getApiSecret());

        Long lastTimestamp = orderRepository.findMaxExecutedAtBySymbol(conn.getUserId(), symbol)
                .map(dt -> dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .orElse(null);

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
            if (tradeService.processNewOrder(dto) != null) {
                count++;
            }
        }
        log.info("SYNC_COMPLETE | Symbol: {} | Added: {} | Duration: {}ms", 
             symbol, count, System.currentTimeMillis() - startTime);
        return count;
    }
}