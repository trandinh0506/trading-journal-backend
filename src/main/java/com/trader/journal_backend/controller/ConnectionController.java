package com.trader.journal_backend.controller;

import com.trader.journal_backend.model.UserExchangeConnection;
import com.trader.journal_backend.service.UserExchangeConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final UserExchangeConnectionService connectionService;

    @PostMapping
    public ResponseEntity<?> addConnection(@RequestBody UserExchangeConnection connection) {
        try {
            connection.setUserId(1L); 
            
            UserExchangeConnection saved = connectionService.createConnection(connection);
            return ResponseEntity.ok("Connection created and verified successfully with ID: " + saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}