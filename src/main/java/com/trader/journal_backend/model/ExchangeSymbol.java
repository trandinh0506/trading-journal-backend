package com.trader.journal_backend.model;

import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private MarketType marketType;

    private String code; 

    private String displayName;

    private String baseAsset;  
    private String quoteAsset; 

    private boolean isActive = true;
}