package com.trader.journal_backend.controller;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.repository.TradeRepository;
import com.trader.journal_backend.repository.UserExchangeConnectionRepository;
import com.trader.journal_backend.service.TradeService;
import com.trader.journal_backend.service.TradeSyncService; // Service gọi API sàn
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeSyncService tradeSyncService;
    private final TradeRepository tradeRepository;
    private final UserExchangeConnectionRepository connectionRepository;

    @GetMapping
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @PostMapping("/order")
    public ResponseEntity<Trade> processOrder(@RequestBody OrderDTO orderDTO) {
        Trade updatedTrade = tradeService.processNewOrder(orderDTO);
        return ResponseEntity.ok(updatedTrade);
    }

    // Endpoint "Cố đấm ăn xôi" để sync trực tiếp từ sàn vào Database
    @PostMapping("/sync/{symbol}")
    public ResponseEntity<String> syncFromExchange(@PathVariable String symbol) {
        // 1. Tìm kết nối của User 1
        UserExchangeConnection conn = connectionRepository.findByUserIdAndIsActiveTrue(1L).stream()
                .filter(c -> c.getMarketType().name().contains("FUTURES"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Futures connection found!"));

        int count = tradeSyncService.syncAndProcess(conn, symbol);
        
        return ResponseEntity.ok("Successfully synced " + count + " orders for " + symbol);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(@PathVariable Long id) {
        return tradeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}