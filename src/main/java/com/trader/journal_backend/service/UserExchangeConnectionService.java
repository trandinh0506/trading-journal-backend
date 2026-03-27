package com.trader.journal_backend.service;

import com.trader.journal_backend.dto.ConnectionResponse;
import com.trader.journal_backend.dto.UserConnectedMetadata;
import com.trader.journal_backend.exchange.ExchangeFactory;
import com.trader.journal_backend.exchange.ExchangeProvider;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.model.enums.ExchangePlatform;
import com.trader.journal_backend.repository.UserExchangeConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserExchangeConnectionService {

    private final UserExchangeConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;
    private final ExchangeFactory exchangeFactory;

    @Transactional
    public UserExchangeConnection createConnection(UserExchangeConnection connection) {
        log.info("CONNECTION_ATTEMPT | Platform: {} | Market: {} | UserID: {}",
                connection.getPlatform(), connection.getMarketType(), connection.getUserId());

        boolean isValid = verifyWithExchange(connection);

        if (!isValid) {
            log.error("CONNECTION_REJECTED | Invalid API Credentials for Platform: {}", connection.getPlatform());
            throw new RuntimeException(
                    "Could not connect to " + connection.getPlatform() + ". Please check your API Key and Secret.");
        }

        String encryptedSecret = encryptionService.encrypt(connection.getApiSecret());
        connection.setApiSecret(encryptedSecret);

        UserExchangeConnection saved = connectionRepository.save(connection);

        log.info("CONNECTION_SUCCESS | Saved Connection ID: {} for User: {}", saved.getId(), saved.getUserId());
        return saved;
    }

    private boolean verifyWithExchange(UserExchangeConnection conn) {
        try {
            ExchangeProvider provider = exchangeFactory.getProvider(conn.getPlatform());

            return provider.testConnection(
                    conn.getApiKey(),
                    conn.getApiSecret(),
                    conn.getMarketType());
        } catch (Exception e) {
            log.error("CONNECTION_VERIFY_EXCEPTION | Platform: {} | Error: {}", conn.getPlatform(), e.getMessage());
            return false;
        }
    }

    public List<ConnectionResponse> getUserConnections(Long userId) {
        return connectionRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(conn -> ConnectionResponse.builder()
                        .id(conn.getId())
                        .exchangeName(conn.getPlatform().name())
                        .marketType(conn.getMarketType().name())
                        .apiKeyMasked(maskApiKey(conn.getApiKey()))
                        .active(conn.isActive())
                        .lastSyncAt(conn.getLastSyncAt())
                        .build())
                .toList();
    }

    public List<UserConnectedMetadata> getUserConnectedMetadata(Long userId) {
        List<UserExchangeConnection> connections = connectionRepository.findByUserIdAndIsActiveTrue(userId);

        Map<ExchangePlatform, List<UserExchangeConnection>> grouped = connections.stream()
                .collect(Collectors.groupingBy(UserExchangeConnection::getPlatform));

        return grouped.entrySet().stream()
                .map(entry -> UserConnectedMetadata.builder()
                        .platform(entry.getKey().name())
                        .markets(entry.getValue().stream()
                                .map(conn -> new UserConnectedMetadata.ConnectedMarket(
                                        conn.getId(), 
                                        conn.getMarketType().name()))
                                .toList())
                        .build())
                .toList();
    }
    
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}