package com.trader.journal_backend.model.enums;

import lombok.Getter;

@Getter
public enum ExchangePlatform {
    BINANCE(1, "Binance"),
    BYBIT(2, "Bybit"),
    OKX(3, "OKX");

    private final int id;
    private final String displayName;

    ExchangePlatform(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    
    public static ExchangePlatform fromId(int id) {
        for (ExchangePlatform p : values()) {
            if (p.id == id) return p;
        }
        throw new IllegalArgumentException("Unknown Exchange ID: " + id);
    }
}