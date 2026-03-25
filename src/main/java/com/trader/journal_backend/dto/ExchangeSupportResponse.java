package com.trader.journal_backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trader.journal_backend.model.enums.MarketType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeSupportResponse {
    private int id;
    private String code;
    private String name;
    @JsonProperty("market_types")
    private List<MarketType> marketTypes;
}