package com.trader.journal_backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderResponseDTO {
    private Long id;
    private String side;
    private String price;
    private String volume;
    private String realizedPnl;
    private String fee;
    private String feeAsset;
    private String externalOrderId;
    private LocalDateTime executedAt;
}