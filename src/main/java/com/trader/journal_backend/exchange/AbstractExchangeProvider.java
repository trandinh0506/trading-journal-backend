package com.trader.journal_backend.exchange;

public abstract class AbstractExchangeProvider implements ExchangeProvider {
    protected void logEvent(String message) {
        System.out.println("[" + getExchange() + "] " + message);
    }
    
}
