package com.trader.journal_backend.exchange.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.exchange.AbstractExchangeProvider;
import com.trader.journal_backend.model.ExchangeSymbol;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BinanceProvider extends AbstractExchangeProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.env:production}")
    private String appEnv;

    @Override
    public ExchangePlatform getExchange() {
        return ExchangePlatform.BINANCE;
    }

    @Override
    public List<ExchangeSymbol> fetchAvailableSymbols(MarketType marketType) {
        long startTime = System.currentTimeMillis();
        log.info("SYMBOLS_SYNC_START | Platform: {} | Market: {}", getExchange(), marketType);

        try {
            String jsonResponse;
            if (marketType == MarketType.SPOT) {
                jsonResponse = new SpotClientImpl().createMarket().exchangeInfo(new LinkedHashMap<>());
            } else {
                jsonResponse = new UMFuturesClientImpl().market().exchangeInfo();
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode symbolsNode = rootNode.get("symbols");

            if (symbolsNode == null || !symbolsNode.isArray()) {
                log.error("API_STRUCTURE_ERROR | Platform: {} | Field 'symbols' not found or not an array", getExchange());
                return Collections.emptyList();
            }

            List<ExchangeSymbol> symbols = new ArrayList<>();
            for (JsonNode s : symbolsNode) {
                if (!s.path("status").isMissingNode() && "TRADING".equals(s.get("status").asText())) {
                    ExchangeSymbol entity = new ExchangeSymbol();
                    entity.setExchange(getExchange());
                    entity.setMarketType(marketType);
                    entity.setCode(s.get("symbol").asText());
                    entity.setDisplayName(s.get("baseAsset").asText() + "/" + s.get("quoteAsset").asText());
                    entity.setBaseAsset(s.get("baseAsset").asText());
                    entity.setQuoteAsset(s.get("quoteAsset").asText());
                    symbols.add(entity);
                }
            }

            log.info("SYMBOLS_SYNC_SUCCESS | Market: {} | Count: {} | Duration: {}ms", 
                    marketType, symbols.size(), System.currentTimeMillis() - startTime);
            return symbols;

        } catch (Exception e) {
            log.error("SYMBOLS_SYNC_FAILED | Platform: {} | Error: {}", getExchange(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<OrderDTO> fetchTradeHistory(String apiKey, String secretKey, String symbol, MarketType marketType, Long startTime) {
        log.info("FETCH_HISTORY | Symbol: {} | Market: {} | StartTime: {}", symbol, marketType, startTime);
        
        List<OrderDTO> allOrders = new ArrayList<>();
        long now = System.currentTimeMillis();
        long windowMillis = 7 * 24 * 60 * 60 * 1000L; // 7 days for each batch
        
        // If startTime is null, we want to fetch the most recent 7 days of history, so we set currentStart to now - windowMillis
        long currentStart = (startTime != null) ? (startTime + 1) : (now - windowMillis);
        log.info("CurrentStart: {} | Now: {} | CurrentStart - Now: {}", currentStart, now, currentStart - now);
        try {
            while (currentStart < now) {
                // Each batch will cover a window of time defined by windowMillis
                long currentEnd = Math.min(currentStart + windowMillis, now);
                
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", symbol);
                parameters.put("startTime", currentStart);
                parameters.put("endTime", currentEnd);
                parameters.put("limit", 1000);

                String response;
                if (marketType == MarketType.SPOT) {
                    response = new SpotClientImpl(apiKey, secretKey).createTrade().myTrades(new LinkedHashMap<>(parameters));
                } else {
                    // Futures API: userTrades/accountTradeList
                    response = new UMFuturesClientImpl(apiKey, secretKey).account().accountTradeList(new LinkedHashMap<>(parameters));
                }

                List<Map<String, Object>> batch = objectMapper.readValue(response, new TypeReference<>() {});
                
                if (!batch.isEmpty()) {
                    List<OrderDTO> batchOrders = batch.stream()
                            .map(t -> mapToOrderDTO(t, symbol, marketType))
                            .collect(Collectors.toList());
                    allOrders.addAll(batchOrders);

                    // Take the timestamp of the last trade in this batch to set the next start time, adding 1ms to avoid overlap
                    long lastTradeTime = (long) batch.get(batch.size() - 1).get("time");
                    currentStart = lastTradeTime + 1;
                } else {
                    // If this batch has no trades, jump the time window to currentEnd
                    currentStart = currentEnd + 1;
                }

                // Avoid spamming too quickly and getting IP banned by Binance (Rate Limit)
                if (currentStart < now) Thread.sleep(150); 
            }

            log.info("FETCH_HISTORY_SUCCESS | Symbol: {} | Total Fetched: {}", symbol, allOrders.size());
            return allOrders;

        } catch (Exception e) {
            log.error("FETCH_HISTORY_FAILED | Symbol: {} | Error: {}", symbol, e.getMessage());
            return Collections.emptyList();
        }
    }

    private OrderDTO mapToOrderDTO(Map<String, Object> t, String symbol, MarketType marketType) {
        try {
            OrderDTO dto = new OrderDTO();
            dto.setSymbol(symbol);
            
            if (t.containsKey("side")) {
                dto.setSide(t.get("side").toString().toUpperCase());
            } else {
                dto.setSide((boolean) t.get("isBuyer") ? "BUY" : "SELL");
            }

            dto.setPrice(new BigDecimal(t.get("price").toString()));
            dto.setVolume(new BigDecimal(t.get("qty").toString()));
            dto.setRealizedPnl(new BigDecimal(t.get("realizedPnl").toString()));
            dto.setCommission(new BigDecimal(t.get("commission").toString()));
            dto.setCommissionAsset(t.get("commissionAsset").toString());
            
            long ts = (long) t.get("time");
            dto.setExecutedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), java.time.ZoneOffset.UTC));
            
            dto.setExternalOrderId(t.get("id").toString());
            
            return dto;
        } catch (Exception e) {
            log.warn("MAPPING_ORDER_ERROR | Symbol: {} | Data: {} | Error: {}", symbol, t, e.getMessage());
            return null;
        }
    }

    @Override
    public void startRealtimeListener(String apiKey, String secretKey, List<String> symbols, MarketType marketType) {
        log.info("REALTIME_LISTENER_START | Symbols: {} | Market: {}", symbols, marketType);
    }

    @Override
    public boolean testConnection(String apiKey, String secretKey, MarketType marketType) {
        try {
            log.info("SECURITY_CHECK | Platform: BINANCE | Market: {} | Self-Host Mode Audit...", marketType);
            
            SpotClientImpl client = new SpotClientImpl(apiKey, secretKey);
            String permissionJson = client.createWallet().apiPermission(new LinkedHashMap<>());
            JsonNode node = objectMapper.readTree(permissionJson);
            
            // block withdrawing or internal transfer permissions for security
            if (node.path("enableWithdrawals").asBoolean(false) || 
                node.path("enableInternalTransfer").asBoolean(false)) {
                throw new RuntimeException("Insecure API Key: Withdrawing or internal transfer permissions must be disabled.");
            }

            // check enable reading permission
            if (!node.path("enableReading").asBoolean(false)) {
                throw new RuntimeException("API Key lacks 'Enable Reading' permission.");
            }

            // check trading permissions based on market type
            if (marketType == MarketType.SPOT) {
                boolean canTradeSpot = node.path("enableSpotAndMarginTrading").asBoolean(false);
                if (!canTradeSpot) {
                    log.warn("TRADING_DISABLED | Spot Trading permission is disabled. You can only view history, not place orders.");
                }
                // Verify Spot Key
                client.createTrade().account(new LinkedHashMap<>());
                
            } else if (marketType == MarketType.FUTURES) {
                boolean canTradeFutures = node.path("enableFutures").asBoolean(false);
                if (!canTradeFutures) {
                    log.warn("TRADING_DISABLED | Futures Trading permission is disabled. You can only view history, not place orders.");
                }
                // Verify Futures Key
                new UMFuturesClientImpl(apiKey, secretKey).account().accountInformation(new LinkedHashMap<>());
            }

            log.info("BINANCE_CONNECTION_VERIFIED | Market: {} | Trading Enabled: {}", marketType, true);
            return true;

        } catch (Exception e) {
            log.error("CONNECTION_FAILED | Error: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public List<MarketType> getSupportedMarketTypes() {
        return List.of(MarketType.SPOT, MarketType.FUTURES);
    }
}