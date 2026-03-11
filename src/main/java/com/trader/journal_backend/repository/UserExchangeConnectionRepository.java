package com.trader.journal_backend.repository;

import com.trader.journal_backend.model.UserExchangeConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserExchangeConnectionRepository extends JpaRepository<UserExchangeConnection, Long> {
    List<UserExchangeConnection> findByUserIdAndIsActiveTrue(Long userId);
}