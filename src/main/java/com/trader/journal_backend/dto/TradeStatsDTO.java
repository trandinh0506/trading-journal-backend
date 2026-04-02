package com.trader.journal_backend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeStatsDTO {
    @JsonProperty("total_trades")
    private long totalTrades;

    @JsonProperty("win_rate")
    private double winRate;

    @JsonProperty("total_pnl")
    private BigDecimal totalPnl;

    @JsonProperty("total_fee")
    private BigDecimal totalFee;

    @JsonProperty("net_pnl")
    private BigDecimal netPnl;

    @JsonProperty("avg_win")
    private double avgWin;

    @JsonProperty("avg_loss")
    private double avgLoss;
}
