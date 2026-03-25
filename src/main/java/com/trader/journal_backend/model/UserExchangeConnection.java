package com.trader.journal_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.model.enums.MarketType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_exchange_connections")
@Data
public class UserExchangeConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangePlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("market_type")
    private MarketType marketType;

    @Column(name = "api_key", nullable = false)
    @JsonProperty("api_key")
    private String apiKey;

    @Column(name = "api_secret", nullable = false)
    @JsonProperty("api_secret")
    private String apiSecret;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    private boolean isActive = true;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}