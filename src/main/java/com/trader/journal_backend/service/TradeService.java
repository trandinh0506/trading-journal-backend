package com.trader.journal_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

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

        // 2. Tạo lệnh con (Order)
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
        
        BigDecimal totalValue = orders.stream()
            .map(o -> o.getPrice().multiply(o.getVolume()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVol = orders.stream()
            .map(Order::getVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        trade.setAverageEntryPrice(totalValue.divide(totalVol, 8, RoundingMode.HALF_UP));
        trade.setTotalVolume(totalVol);

        BigDecimal firstSl = orders.get(0).getSl();
        boolean allSlSame = orders.stream().allMatch(o -> Objects.equals(o.getSl(), firstSl));
        trade.setCurrentSl(allSlSame ? firstSl : null);

        BigDecimal firstTp = orders.get(0).getTp();
        boolean allTpSame = orders.stream().allMatch(o -> Objects.equals(o.getTp(), firstTp));
        trade.setCurrentTp(allTpSame ? firstTp : null);
    }
}