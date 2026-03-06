package com.trader.journal_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trader.journal_backend.dto.OrderDTO;
import com.trader.journal_backend.model.Order;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.repository.OrderRepository;
import com.trader.journal_backend.repository.TradeRepository;

import jakarta.transaction.Transactional;

@Service
public class TradeService {
    @Autowired private TradeRepository tradeRepository;
    @Autowired private OrderRepository orderRepository;

    @Transactional
    public Trade processNewOrder(OrderDTO dto) {
        Trade trade = tradeRepository.findBySymbolAndSideAndStatus(dto.getSymbol(), dto.getSide(), "OPEN")
                .orElse(new Trade());

        if (trade.getId() == null) {
            trade.setSymbol(dto.getSymbol());
            trade.setSide(dto.getSide());
            trade.setStatus("OPEN");
            trade = tradeRepository.save(trade);
        }

        Order order = new Order();
        order.setTrade(trade);
        order.setPrice(dto.getPrice());
        order.setVolume(dto.getVolume());
        order.setSl(dto.getSl());
        order.setTp(dto.getTp());
        orderRepository.save(order);

        updateTradeSummary(trade);
        
        return tradeRepository.save(trade);
    }

    private void updateTradeSummary(Trade trade) {
        List<Order> orders = orderRepository.findByTradeId(trade.getId());
        if (orders.isEmpty()) return;

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalVol = BigDecimal.ZERO;

        for (Order o : orders) {
            totalValue = totalValue.add(o.getPrice().multiply(o.getVolume()));
            totalVol = totalVol.add(o.getVolume());
        }

        if (totalVol.compareTo(BigDecimal.ZERO) > 0) {
            trade.setAverageEntryPrice(totalValue.divide(totalVol, 8, RoundingMode.HALF_UP));
            trade.setTotalVolume(totalVol);
        }

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