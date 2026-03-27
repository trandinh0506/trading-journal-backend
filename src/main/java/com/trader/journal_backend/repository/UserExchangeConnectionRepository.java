package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserExchangeConnectionRepository extends JpaRepository<UserExchangeConnection, Long> {
    List<UserExchangeConnection> findByUserIdAndIsActiveTrue(Long userId);
    List<UserExchangeConnection> findByUserIdAndPlatformAndMarketTypeAndIsActiveTrue(
            Long userId, ExchangePlatform platform, MarketType marketType);
}