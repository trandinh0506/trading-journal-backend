package com.trader.journal_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EquityPointDTO {
    private LocalDateTime timestamp;
    
    @JsonProperty("net_pnl")
    private BigDecimal netPnl;
    
    @JsonProperty("cumulative_pnl")
    private BigDecimal cumulativePnl;
}