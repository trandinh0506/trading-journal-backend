package com.trader.journal_backend.controller;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.service.TradeService;
import com.trader.journal_backend.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TradeRepository tradeRepository;

    @GetMapping
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @PostMapping("/order")
    public ResponseEntity<Trade> processOrder(@RequestBody OrderDTO orderDTO) {
        Trade updatedTrade = tradeService.processNewOrder(orderDTO);
        return ResponseEntity.ok(updatedTrade);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(@PathVariable Long id) {
        return tradeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}