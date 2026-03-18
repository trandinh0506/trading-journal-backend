package com.trader.journal_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TradeResponseDTO {
    private Long id;
    private String symbol;
    private String side;
    private String status;
    private String averageEntryPrice;
    private String totalVolume;         
    private String totalExecutedVolume; 
    private String totalEntryVolume; 
    private String totalRealizedPnl;
    private List<TradeImageResponseDTO> images;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private List<OrderResponseDTO> orders;
}