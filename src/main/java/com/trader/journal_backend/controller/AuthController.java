package com.trader.journal_backend.controller;


import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.trader.journal_backend.dto.LoginRequest;
import com.trader.journal_backend.dto.RegisterRequest;
import com.trader.journal_backend.dto.TokenResponse;
import com.trader.journal_backend.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Client-Type", defaultValue = "web") String clientType,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.login(request);

        if ("web".equalsIgnoreCase(clientType)) {
            ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of(
                "access_token", tokenResponse.getAccessToken(),
                "user", Map.of("email", tokenResponse.getEmail(), "id", tokenResponse.getUserId(), "full_name", tokenResponse.getFullName())
            ));
        } else {
            return ResponseEntity.ok(tokenResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestHeader(value = "X-Client-Type", defaultValue = "web") String clientType,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletResponse response) {

        String token = "web".equalsIgnoreCase(clientType) ? 
                       refreshTokenFromCookie : (body != null ? body.get("refreshToken") : null);

        if (token == null) {
            return ResponseEntity.status(401).body("Refresh token is missing");
        }

        TokenResponse tokenResponse = authService.refreshTokens(token);

        if ("web".equalsIgnoreCase(clientType)) {
            ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of(
                "access_token", tokenResponse.getAccessToken()
            ));
        }

        return ResponseEntity.ok(tokenResponse);
    }

    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();
    }
}