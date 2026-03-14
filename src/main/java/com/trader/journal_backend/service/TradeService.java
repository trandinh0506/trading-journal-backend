package com.trader.journal_backend.service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.dto.OrderResponseDTO;
import com.trader.journal_backend.dto.TradeResponseDTO;
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
import java.util.Comparator;
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

        // Find an opposite OPEN trade to close/reduce position first
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
        order.setRealizedPnl(dto.getRealizedPnl());
        order.setFee(dto.getCommission()); 
        order.setFeeAsset(dto.getCommissionAsset());
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

    public void updateTradeSummary(Trade trade) {
        List<Order> orders = orderRepository.findByTradeId(trade.getId());
        if (orders.isEmpty()) return;

        orders.sort(Comparator.comparing(Order::getExecutedAt));

        BigDecimal currentVol = BigDecimal.ZERO;     
        BigDecimal totalExecuted = BigDecimal.ZERO;  
        BigDecimal entryVol = BigDecimal.ZERO;       
        BigDecimal entryValue = BigDecimal.ZERO;     
        BigDecimal totalPnL = BigDecimal.ZERO;

        for (Order o : orders) {
            BigDecimal vol = o.getVolume();
            totalExecuted = totalExecuted.add(vol);
            
            if (o.getRealizedPnl() != null) {
                totalPnL = totalPnL.add(o.getRealizedPnl());
            }

            if (o.getSide().equalsIgnoreCase(trade.getSide())) {
                currentVol = currentVol.add(vol);
                entryVol = entryVol.add(vol);
                entryValue = entryValue.add(o.getPrice().multiply(vol));
            } else {
                currentVol = currentVol.subtract(vol);
            }
        }

        trade.setTotalVolume(currentVol.stripTrailingZeros());
        trade.setTotalExecutedVolume(totalExecuted.stripTrailingZeros());
        trade.setTotalEntryVolume(entryVol.stripTrailingZeros());
        trade.setTotalRealizedPnl(totalPnL.stripTrailingZeros());

        if (entryVol.compareTo(BigDecimal.ZERO) > 0) {
            trade.setAverageEntryPrice(entryValue.divide(entryVol, 8, RoundingMode.HALF_UP).stripTrailingZeros());
        }

        trade.setOpenedAt(orders.get(0).getExecutedAt());
        if (currentVol.abs().compareTo(new BigDecimal("0.00000001")) <= 0) {
            trade.setStatus("CLOSED");
            trade.setClosedAt(orders.get(orders.size() - 1).getExecutedAt());
            trade.setTotalVolume(BigDecimal.ZERO); 
        } else {
            trade.setStatus("OPEN");
            trade.setClosedAt(null);
        }
    }
    public TradeResponseDTO convertToResponseDTO(Trade trade) {
        TradeResponseDTO dto = new TradeResponseDTO();
        dto.setId(trade.getId());
        dto.setSymbol(trade.getSymbol());
        dto.setSide(trade.getSide());
        dto.setStatus(trade.getStatus());
        dto.setOpenedAt(trade.getOpenedAt());
        dto.setClosedAt(trade.getClosedAt());

        dto.setAverageEntryPrice(format(trade.getAverageEntryPrice()));
        dto.setTotalVolume(format(trade.getTotalVolume()));
        dto.setTotalExecutedVolume(format(trade.getTotalExecutedVolume()));
        dto.setTotalEntryVolume(format(trade.getTotalEntryVolume()));
        dto.setTotalRealizedPnl(format(trade.getTotalRealizedPnl()));

        if (trade.getOrders() != null) {
            dto.setOrders(trade.getOrders().stream().map(o -> {
                OrderResponseDTO odto = new OrderResponseDTO();
                odto.setId(o.getId());
                odto.setSide(o.getSide());
                odto.setPrice(format(o.getPrice()));
                odto.setVolume(format(o.getVolume()));
                odto.setRealizedPnl(format(o.getRealizedPnl()));
                odto.setFee(format(o.getFee()));
                odto.setFeeAsset(o.getFeeAsset());
                odto.setExternalOrderId(o.getExternalOrderId());
                odto.setExecutedAt(o.getExecutedAt());
                return odto;
            }).toList());
        }
        return dto;
    }

    private String format(BigDecimal value) {
        if (value == null) return "0";
        return value.stripTrailingZeros().toPlainString();
    }
}