package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.entity.RefreshToken;
import com.authservice.entity.User;
import com.authservice.repository.RefreshTokenRepository;
import com.authservice.repository.UserRepository;
import com.authservice.security.HashUtils;
import com.authservice.security.JwtService;
import com.authservice.security.RedisRevocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

/**
 * Controller for token refresh and revocation endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RefreshController {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final HashUtils hashUtils;
    private final RedisRevocationService redisRevocationService;

    /**
     * Refresh access token using a valid refresh token.
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<JwtIssue> refresh(@Valid @RequestBody RefreshRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        String refreshTokenHash = hashUtils.sha256(refreshTokenValue);

        // Find refresh token in database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(refreshTokenHash);

        if (refreshTokenOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        // Check if revoked
        if (refreshToken.getRevoked()) {
            return ResponseEntity.status(401).build();
        }

        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).build();
        }

        // Get username from user ID
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String username = user.getUsername();

        // Issue new tokens (optionally rotate refresh token)
        JwtIssue newTokens = jwtService.issueTokens(username);

        // Optionally revoke old refresh token (token rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(newTokens);
    }

    /**
     * Revoke access token and/or refresh token.
     * POST /auth/revoke
     */
    @PostMapping("/revoke")
    @Transactional
    public ResponseEntity<Void> revoke(@RequestBody RevokeRequest request) {
        // Revoke access token if provided
        if (request.getAccessToken() != null && !request.getAccessToken().isEmpty()) {
            Optional<Jwt> jwtOpt = jwtService.validateToken(request.getAccessToken());
            if (jwtOpt.isPresent()) {
                Jwt jwt = jwtOpt.get();
                String jti = jwt.getClaimAsString("jti");
                Instant expiresAt = jwt.getExpiresAt();
                if (jti != null && expiresAt != null) {
                    long expiryEpochMillis = expiresAt.toEpochMilli();
                    redisRevocationService.revokeTokenJti(jti, expiryEpochMillis);
                }
            }
        }

        // Revoke refresh token if provided
        if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            String refreshTokenHash = hashUtils.sha256(request.getRefreshToken());
            refreshTokenRepository.findByTokenHash(refreshTokenHash)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }

        return ResponseEntity.ok().build();
    }
}

