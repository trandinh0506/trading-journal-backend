package com.trader.journal_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trader.journal_backend.dto.EquityPointDTO;
import com.trader.journal_backend.dto.TradeStatsDTO;
import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.repository.TradeRepository;

@Service
public class TradeStatsService {
    @Autowired
    private TradeRepository tradeRepository;

    public TradeStatsDTO getStats(Long userId, Integer month, Integer quarter, Integer year) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (year != null) {
            if (month != null) {
                start = LocalDateTime.of(year, month, 1, 0, 0);
                end = start.plusMonths(1).minusNanos(1);
            } else if (quarter != null) {
                start = LocalDateTime.of(year, (quarter - 1) * 3 + 1, 1, 0, 0);
                end = start.plusMonths(3).minusNanos(1);
            } else {
                start = LocalDateTime.of(year, 1, 1, 0, 0);
                end = start.plusYears(1).minusNanos(1);
            }
        }

        Map<String, Object> res = tradeRepository.getRawStats(userId, start, end);

        BigDecimal totalPnl = new BigDecimal(res.get("total_pnl").toString());
        BigDecimal totalFee = new BigDecimal(res.get("total_fee").toString());

        return new TradeStatsDTO(
                ((Number) res.get("total_trades")).longValue(),
                ((Number) res.get("win_rate")).doubleValue(),
                totalPnl,
                totalFee,
                totalPnl.subtract(totalFee),
                ((Number) res.get("avg_win")).doubleValue(),
                ((Number) res.get("avg_loss")).doubleValue());
    }

    public List<EquityPointDTO> getEquityCurve(Long userId) {
        List<Trade> trades = tradeRepository.findAllClosedTrades(userId);
        List<EquityPointDTO> equityCurve = new ArrayList<>();
        
        BigDecimal cumulative = BigDecimal.ZERO;

        for (Trade t : trades) {
            BigDecimal totalFee = t.getOrders().stream()
                    .map(order -> order.getFee() != null ? order.getFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal netPnl = t.getTotalRealizedPnl().subtract(totalFee);
            cumulative = cumulative.add(netPnl);

            equityCurve.add(new EquityPointDTO(
                t.getClosedAt(),
                netPnl.stripTrailingZeros(),
                cumulative.stripTrailingZeros()
            ));
        }
        return equityCurve;
    }

}