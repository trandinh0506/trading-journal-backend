package com.trader.journal_backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionResponse {
    private Long id;

    @JsonProperty("exchange_name")
    private String exchangeName;

    @JsonProperty("market_type")
    private String marketType;

    @JsonProperty("api_key_masked")
    private String apiKeyMasked;

    @JsonProperty("is_active")
    private boolean active;

    @JsonProperty("last_sync_at")
    private LocalDateTime lastSyncAt;
}