package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByUserIdAndSymbolAndSideAndStatus(Long userId, String symbol, String side, String status);
    Optional<Trade> findFirstByUserIdAndSymbolAndStatusOrderByOpenedAtAsc(Long userId, String symbol, String status);
    Optional<Trade> findFirstByUserIdAndSymbolOrderByOpenedAtDesc(Long userId, String symbol);
}