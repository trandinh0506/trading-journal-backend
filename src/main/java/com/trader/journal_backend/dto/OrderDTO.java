package com.trader.journal_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class OrderDTO {
    private String symbol;
    private String side;
    
    private BigDecimal price;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal volume;
    
    private BigDecimal sl;
    private BigDecimal tp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executedAt;

    private String externalOrderId;

    private String marketType;
    
    private BigDecimal commission;
    private String commissionAsset;
    private BigDecimal realizedPnl;

}