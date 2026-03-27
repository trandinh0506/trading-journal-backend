package com.trader.journal_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "exchange_symbols")
@Data
public class ExchangeSymbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ExchangePlatform exchange;

    @Enumerated(EnumType.STRING)
    @JsonProperty("market_type")
    private MarketType marketType;

    private String code; 

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("base_asset")
    private String baseAsset;  

    @JsonProperty("quote_asset")
    private String quoteAsset; 

    @JsonProperty("is_active")
    private boolean isActive = true;

    @JsonProperty("is_active")
    public boolean isActive() {
        return isActive;
    }
}