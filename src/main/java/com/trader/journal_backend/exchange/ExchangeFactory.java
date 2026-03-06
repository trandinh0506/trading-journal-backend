package com.trader.journal_backend.exchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExchangeFactory {
    private final Map<String, ExchangeProvider> providers = new HashMap<>();

    @Autowired
    public ExchangeFactory(List<ExchangeProvider> providerList) {
        providerList.forEach(p -> providers.put(p.getExchangeName().toUpperCase(), p));
    }

    public ExchangeProvider getProvider(String exchangeName) {
        ExchangeProvider provider = providers.get(exchangeName.toUpperCase());
        if (provider == null) {
            throw new RuntimeException("Exchange " + exchangeName + " is not supported!");
        }
        return provider;
    }
}