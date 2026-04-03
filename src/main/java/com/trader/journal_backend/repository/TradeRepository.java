package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByUserIdAndSymbolAndSideAndStatus(Long userId, String symbol, String side, String status);

    Optional<Trade> findFirstByUserIdAndSymbolAndStatusOrderByOpenedAtAsc(Long userId, String symbol, String status);

    Optional<Trade> findFirstByUserIdAndSymbolOrderByOpenedAtDesc(Long userId, String symbol);

    @Query(value = """
                SELECT
                    COUNT(id) as total_trades,
                    CASE WHEN COUNT(id) > 0 THEN
                        (CAST(SUM(CASE WHEN total_realized_pnl > 0 THEN 1 ELSE 0 END) AS NUMERIC) * 100.0 / COUNT(id))
                    ELSE 0 END as win_rate,
                    COALESCE(SUM(total_realized_pnl), 0) as total_pnl,
                    COALESCE((SELECT SUM(o.fee) FROM trading_orders o WHERE o.trade_id IN (SELECT t2.id FROM trades t2 WHERE t2.user_id = :userId AND t2.status = 'CLOSED')), 0) as total_fee,
                    COALESCE(AVG(CASE WHEN total_realized_pnl > 0 THEN total_realized_pnl END), 0) as avg_win,
                    COALESCE(AVG(CASE WHEN total_realized_pnl < 0 THEN total_realized_pnl END), 0) as avg_loss
                FROM trades
                WHERE user_id = :userId
                AND status = 'CLOSED'
                AND (CAST(:startDate AS TIMESTAMP) IS NULL OR closed_at >= CAST(:startDate AS TIMESTAMP))
                AND (CAST(:endDate AS TIMESTAMP) IS NULL OR closed_at <= CAST(:endDate AS TIMESTAMP))
            """, nativeQuery = true)
    Map<String, Object> getRawStats(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
            

    @Query("""
                SELECT t FROM Trade t
                WHERE t.userId = :userId AND t.status = 'CLOSED'
                ORDER BY t.closedAt ASC
            """)
    List<Trade> findAllClosedTrades(@Param("userId") Long userId);
}