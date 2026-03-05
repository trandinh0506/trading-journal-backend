package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {
    List<TradeImage> findByTradeId(Long tradeId);
}