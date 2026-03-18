package com.trader.journal_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeImageResponseDTO {
    private String type;
    private String url;
}