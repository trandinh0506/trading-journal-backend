package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTradeId(Long tradeId);
    
    boolean existsByExternalOrderId(String externalOrderId);

    @Query("SELECT MAX(o.executedAt) FROM Order o WHERE o.trade.userId = :userId AND o.trade.symbol = :symbol")
    Optional<LocalDateTime> findMaxExecutedAtBySymbol(@Param("userId") Long userId, @Param("symbol") String symbol);
}