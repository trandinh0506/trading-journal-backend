package com.trader.journal_backend.controller;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.dto.TradeResponseDTO;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.repository.TradeRepository;
import com.trader.journal_backend.repository.UserExchangeConnectionRepository;
import com.trader.journal_backend.security.UserPrincipal;
import com.trader.journal_backend.service.TradeService;
import com.trader.journal_backend.service.TradeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeSyncService tradeSyncService;
    private final TradeRepository tradeRepository;
    private final UserExchangeConnectionRepository connectionRepository;

    @GetMapping
    public List<TradeResponseDTO> getAllTrades() {
        return tradeRepository.findAll().stream()
                .map(tradeService::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/order")
    public ResponseEntity<TradeResponseDTO> processOrder(@RequestBody OrderDTO orderDTO) {
        Trade updatedTrade = tradeService.processNewOrder(orderDTO);
        if (updatedTrade == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tradeService.convertToResponseDTO(updatedTrade));
    }

    @PostMapping("/sync/{symbol}")
    public ResponseEntity<String> syncFromExchange(@PathVariable String symbol, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserExchangeConnection conn = connectionRepository.findByUserIdAndIsActiveTrue(userPrincipal.getId()).stream()
                .filter(c -> c.getMarketType().name().contains("FUTURES"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Futures connection found!"));

        int count = tradeSyncService.syncAndProcess(conn, symbol);
        
        return ResponseEntity.ok("Successfully synced " + count + " orders for " + symbol);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeResponseDTO> getTradeById(@PathVariable Long id) {
        return tradeRepository.findById(id)
                .map(trade -> ResponseEntity.ok(tradeService.convertToResponseDTO(trade)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recalculate")
    public ResponseEntity<String> recalculateAll() {
        List<Trade> allTrades = tradeRepository.findAll();
        allTrades.forEach(tradeService::updateTradeSummary);
        tradeRepository.saveAll(allTrades);
        return ResponseEntity.ok("Recalculated " + allTrades.size() + " trades.");
    }
}