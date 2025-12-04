package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.entity.RefreshToken;
import com.authservice.entity.User;
import com.authservice.repository.RefreshTokenRepository;
import com.authservice.repository.UserRepository;
import com.authservice.security.HashUtils;
import com.authservice.security.JwtService;
import com.authservice.security.RateLimiterService;
import com.authservice.security.RedisRevocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final RateLimiterService rateLimiterService;

    /**
     * Refresh access token using a valid refresh token.
     * Implements refresh token rotation: old token is revoked, new token is issued.
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.getRefreshToken();
        String refreshTokenHash = hashUtils.sha256(refreshTokenValue);
        String ipAddress = getClientIpAddress(httpRequest);

        // Find refresh token in database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(refreshTokenHash);

        if (refreshTokenOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        // Check if revoked
        if (refreshToken.getRevoked()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        // Get username from user ID
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String username = user.getUsername();

        // Rate limiting
        if (!rateLimiterService.isRefreshAllowed(username, ipAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later.");
        }

        // Update last used timestamp
        refreshToken.setLastUsedAt(Instant.now());
        refreshToken.setIpAddress(ipAddress);
        refreshTokenRepository.save(refreshToken);

        // Issue new tokens with token rotation (new refresh token)
        JwtIssue newTokens = jwtService.issueTokens(username, refreshToken.getDeviceInfo(), ipAddress);

        // Revoke old refresh token (token rotation)
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
    public ResponseEntity<Void> revoke(@RequestBody RevokeRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        final String[] username = {null};

        // Get username from access token if provided
        if (request.getAccessToken() != null && !request.getAccessToken().isEmpty()) {
            Optional<Jwt> jwtOpt = jwtService.validateToken(request.getAccessToken());
            if (jwtOpt.isPresent()) {
                Jwt jwt = jwtOpt.get();
                username[0] = jwt.getSubject();
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
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(refreshTokenHash);
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                if (username[0] == null) {
                    userRepository.findById(token.getUserId())
                            .ifPresent(user -> username[0] = user.getUsername());
                }
            }
        }

        // Rate limiting (if we have username)
        if (username[0] != null && !rateLimiterService.isRevokeAllowed(username[0], ipAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later.");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Get all active sessions for the authenticated user.
     * GET /auth/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getSessions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());
        List<SessionResponse> sessions = tokens.stream()
                .map(token -> SessionResponse.builder()
                        .sessionId(token.getSessionId())
                        .issuedAt(token.getIssuedAt())
                        .lastUsedAt(token.getLastUsedAt())
                        .expiresAt(token.getExpiresAt())
                        .deviceInfo(token.getDeviceInfo())
                        .ipAddress(token.getIpAddress())
                        .revoked(token.getRevoked())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    /**
     * Delete a specific session by session ID.
     * DELETE /auth/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Transactional
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findBySessionId(sessionId);
        if (tokenOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }

        RefreshToken token = tokenOpt.get();
        if (!token.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's session");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return ResponseEntity.noContent().build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

