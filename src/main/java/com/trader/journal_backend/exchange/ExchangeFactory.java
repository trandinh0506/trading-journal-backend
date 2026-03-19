package com.trader.journal_backend.exchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trader.journal_backend.model.enums.ExchangePlatform;

@Service
public class ExchangeFactory {
    private final Map<ExchangePlatform, ExchangeProvider> providers = new HashMap<>();

    @Autowired
    public ExchangeFactory(List<ExchangeProvider> providerList) {
        providerList.forEach(p -> providers.put(p.getExchange(), p));
    }

    public ExchangeProvider getProvider(ExchangePlatform platform) {
        ExchangeProvider provider = providers.get(platform);
        if (provider == null) {
            throw new RuntimeException("Exchange " + platform + " is not supported yet!");
        }
        return provider;
    }
}