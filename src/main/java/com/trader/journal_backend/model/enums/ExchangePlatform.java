package com.trader.journal_backend.model.enums;

import lombok.Getter;

@Getter
public enum ExchangePlatform {
    BINANCE("Binance"),
    BYBIT("Bybit"),
    OKX("OKX"),
    EXNESS("Exness (Forex)");

    private final String displayName;

    ExchangePlatform(String displayName) {
        this.displayName = displayName;
    }
}