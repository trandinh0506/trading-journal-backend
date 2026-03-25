package com.trader.journal_backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.trader.journal_backend.dto.LoginRequest;
import com.trader.journal_backend.dto.RegisterRequest;
import com.trader.journal_backend.dto.TokenResponse;
import com.trader.journal_backend.model.User;
import com.trader.journal_backend.repository.UserRepository;
import com.trader.journal_backend.util.JwtUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public TokenResponse login(LoginRequest request) {
        log.info("AUTH_LOGIN_ATTEMPT | Email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email or password is incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("AUTH_LOGIN_FAILED | Invalid password for: {}", request.getEmail());
            throw new RuntimeException("Email or password is incorrect");
        }

        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        log.info("AUTH_LOGIN_SUCCESS | User: {}", user.getEmail());
        return new TokenResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getFullName());
    }

    public TokenResponse refreshTokens(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            log.error("AUTH_REFRESH_FAILED | Invalid or expired refresh token");
            throw new RuntimeException("Invalid Refresh Token");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);
        log.info("AUTH_REFRESH_START | Rotating tokens for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtils.generateAccessToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        log.info("AUTH_REFRESH_SUCCESS | Tokens rotated for: {}", email);
        return new TokenResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(), user.getFullName());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("AUTH_REGISTER_SUCCESS | New user registered: {}", user.getEmail());
        return;
    }
}