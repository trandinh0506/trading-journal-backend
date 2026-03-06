package com.trader.journal_backend.exchange;

import com.trader.journal_backend.dto.OrderDTO;
import java.util.List;

public interface ExchangeProvider {
    String getExchangeName();

    List<OrderDTO> fetchTradeHistory(String apiKey, String secretKey, String symbol, Long startTime);

    void startRealtimeListener(String apiKey, String secretKey, String symbol);
}
