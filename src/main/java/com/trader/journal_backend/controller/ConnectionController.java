package com.trader.journal_backend.controller;

import com.trader.journal_backend.dto.ConnectionResponse;
import com.trader.journal_backend.dto.UserConnectedMetadata;
import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.security.UserPrincipal;
import com.trader.journal_backend.service.UserExchangeConnectionService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final UserExchangeConnectionService connectionService;

    @GetMapping
    public ResponseEntity<List<ConnectionResponse>> getMyConnections(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ConnectionResponse> connections = connectionService.getUserConnections(userPrincipal.getId());
        return ResponseEntity.ok(connections);
    }

    @PostMapping
    public ResponseEntity<?> addConnection(@RequestBody UserExchangeConnection connection, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            connection.setUserId(userPrincipal.getId());
            UserExchangeConnection saved = connectionService.createConnection(connection);
            return ResponseEntity.ok("Connection created and verified successfully with ID: " + saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/metadata")
    public ResponseEntity<List<UserConnectedMetadata>> getConnectedMetadata(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(connectionService.getUserConnectedMetadata(userPrincipal.getId()));
    }
}