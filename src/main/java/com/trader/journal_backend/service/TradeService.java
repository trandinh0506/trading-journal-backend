package com.trader.journal_backend.service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.model.Order;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.repository.OrderRepository;
import com.trader.journal_backend.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Trade processNewOrder(OrderDTO dto) {
        log.debug("PROCESS_ORDER | Symbol: {} | Side: {} | Price: {} | Qty: {}", 
              dto.getSymbol(), dto.getSide(), dto.getPrice(), dto.getVolume());

        // Check for duplicate orders using External Order ID
        if (dto.getExternalOrderId() != null && orderRepository.existsByExternalOrderId(dto.getExternalOrderId())) {
            log.info("ORDER_SKIP | Duplicate detected: {}", dto.getExternalOrderId());
            return null; 
        }

        // Strategy: Find an opposite OPEN trade to close/reduce position first
        // If current order is BUY, look for an OPEN SELL trade, and vice versa.
        String oppositeSide = dto.getSide().equalsIgnoreCase("BUY") ? "SELL" : "BUY";
        Optional<Trade> existingTrade = tradeRepository.findBySymbolAndSideAndStatus(dto.getSymbol(), oppositeSide, "OPEN");
        
        Trade trade;
        if (existingTrade.isPresent()) {
            trade = existingTrade.get();
            log.info("TRADE_CLOSE_ACTION | Found opposite open trade ID: {} for {} {}", 
                     trade.getId(), dto.getSide(), dto.getSymbol());
        } else {
            // If no opposite trade, find or create a trade with the SAME side (DCA/New Position)
            trade = tradeRepository.findBySymbolAndSideAndStatus(dto.getSymbol(), dto.getSide(), "OPEN")
                    .orElseGet(() -> {
                        log.info("TRADE_CREATE | No open position found. Creating new Trade for {} {}", dto.getSide(), dto.getSymbol());
                        Trade newTrade = new Trade();
                        newTrade.setSymbol(dto.getSymbol());
                        newTrade.setSide(dto.getSide());
                        newTrade.setStatus("OPEN");
                        return tradeRepository.saveAndFlush(newTrade);
                    });
        }

        // Create and attach new order
        log.info("ORDER_ATTACH | Order {} -> Trade ID: {}", dto.getExternalOrderId(), trade.getId());
        Order order = new Order();
        order.setTrade(trade);
        order.setSide(dto.getSide());
        order.setPrice(dto.getPrice());
        order.setVolume(dto.getVolume());
        order.setSl(dto.getSl());
        order.setTp(dto.getTp());
        order.setExternalOrderId(dto.getExternalOrderId());
        order.setExecutedAt(dto.getExecutedAt());
        orderRepository.save(order);

        // Re-calculate Trade summary (Average Price, Net Volume, Status)
        updateTradeSummary(trade);
        
        log.info("TRADE_UPDATED | ID: {} | Status: {} | TotalVol: {}", 
             trade.getId(), trade.getStatus(), trade.getTotalVolume());
        
        return tradeRepository.save(trade);
    }

    private void updateTradeSummary(Trade trade) {
        List<Order> orders = orderRepository.findByTradeId(trade.getId());
        if (orders.isEmpty()) return;

        BigDecimal netVolume = BigDecimal.ZERO;
        BigDecimal totalEntryValue = BigDecimal.ZERO;
        BigDecimal entryVolume = BigDecimal.ZERO;

        for (Order o : orders) {
            // Logic: If order side matches trade side -> Increase position
            // If order side is opposite -> Decrease position
            if (o.getSide().equalsIgnoreCase(trade.getSide())) {
                netVolume = netVolume.add(o.getVolume());
                totalEntryValue = totalEntryValue.add(o.getPrice().multiply(o.getVolume()));
                entryVolume = entryVolume.add(o.getVolume());
            } else {
                netVolume = netVolume.subtract(o.getVolume());
            }
        }

        // Update Average Entry Price (only based on Entry orders)
        if (entryVolume.compareTo(BigDecimal.ZERO) > 0) {
            trade.setAverageEntryPrice(totalEntryValue.divide(entryVolume, 8, RoundingMode.HALF_UP));
        }
        
        trade.setTotalVolume(netVolume);

        // Check if position is fully closed
        // Using a small threshold to handle potential floating point precision issues
        if (netVolume.compareTo(new BigDecimal("0.00000001")) <= 0) {
            log.info("TRADE_COMPLETED | Trade ID: {} fully closed", trade.getId());
            trade.setStatus("CLOSED");
        }

        // Update SL/TP consistency
        BigDecimal firstSl = orders.get(0).getSl();
        boolean allSlSame = orders.stream()
            .allMatch(o -> (o.getSl() == null && firstSl == null) || 
                        (o.getSl() != null && firstSl != null && o.getSl().compareTo(firstSl) == 0));
        trade.setCurrentSl(allSlSame ? firstSl : null);

        BigDecimal firstTp = orders.get(0).getTp();
        boolean allTpSame = orders.stream()
            .allMatch(o -> (o.getTp() == null && firstTp == null) || 
                        (o.getTp() != null && firstTp != null && o.getTp().compareTo(firstTp) == 0));
        trade.setCurrentTp(allTpSame ? firstTp : null);
    }
}