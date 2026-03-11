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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BinanceProvider extends AbstractExchangeProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        log.debug("FETCH_HISTORY | Symbol: {} | Market: {} | From: {}", symbol, marketType, startTime);
        
        try {
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", symbol);
            if (startTime != null) parameters.put("startTime", startTime + 1);

            String response;
            if (marketType == MarketType.SPOT) {
                response = new SpotClientImpl(apiKey, secretKey).createTrade().myTrades(parameters);
            } else {
                response = new UMFuturesClientImpl(apiKey, secretKey).account().accountTradeList(new LinkedHashMap<>(parameters));
            }

            List<Map<String, Object>> rawTrades = objectMapper.readValue(response, new TypeReference<>() {});
            return rawTrades.stream()
                    .map(t -> mapToOrderDTO(t, symbol, marketType))
                    .collect(Collectors.toList());

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
            
            long ts = (long) t.get("time");
            dto.setExecutedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));
            
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
}