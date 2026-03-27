package com.trader.journal_backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserConnectedMetadata {
    private String platform;
    private List<ConnectedMarket> markets;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConnectedMarket {
        @JsonProperty("connection_id")
        private Long connectionId;

        @JsonProperty("market_type")
        private String marketType;
    }
}