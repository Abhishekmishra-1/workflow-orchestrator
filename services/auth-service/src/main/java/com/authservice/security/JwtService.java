package com.authservice.security;

import com.authservice.dto.JwtIssue;
import com.authservice.entity.RefreshToken;
import com.authservice.entity.User;
import com.authservice.repository.RefreshTokenRepository;
import com.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for JWT token generation, validation, and refresh token management.
 * Uses RS256 (RSA) for signing tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final HashUtils hashUtils;

    @Value("${jwt.access-token-expiry-ms:300000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms:1209600000}")
    private long refreshTokenExpiryMs;

    /**
     * Issue both access and refresh tokens for a user.
     *
     * @param username the username
     * @param deviceInfo optional device information
     * @param ipAddress optional IP address
     * @return JwtIssue containing both tokens and expiry info
     */
    @Transactional
    public JwtIssue issueTokens(String username, String deviceInfo, String ipAddress) {
        // Generate access token with jti claim
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant accessTokenExpiry = now.plusMillis(accessTokenExpiryMs);

        JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .subject(username)
                .issuedAt(now)
                .expiresAt(accessTokenExpiry)
                .claim("jti", jti)
                .claim("type", "access")
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessTokenClaims)).getTokenValue();

        // Generate refresh token (UUID)
        String refreshTokenValue = UUID.randomUUID().toString();
        String refreshTokenHash = hashUtils.sha256(refreshTokenValue);
        Instant refreshTokenExpiry = now.plusMillis(refreshTokenExpiryMs);

        // Get user ID from username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Store refresh token in database with session info
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(refreshTokenHash)
                .userId(user.getId())
                .issuedAt(now)
                .expiresAt(refreshTokenExpiry)
                .revoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .lastUsedAt(now)
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("Issued tokens for user={} jti={} sessionId={} refreshHashPrefix={}", 
                user.getId(), jti, refreshToken.getSessionId(), 
                refreshTokenHash.substring(0, Math.min(12, refreshTokenHash.length())));

        return JwtIssue.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .accessTokenExpiresIn(accessTokenExpiryMs)
                .refreshTokenExpiresIn(refreshTokenExpiryMs)
                .build();
    }

    /**
     * Issue both access and refresh tokens for a user (backward compatibility).
     *
     * @param username the username
     * @return JwtIssue containing both tokens and expiry info
     */
    @Transactional
    public JwtIssue issueTokens(String username) {
        return issueTokens(username, null, null);
    }

    /**
     * Validate and decode a JWT token.
     *
     * @param token the JWT token string
     * @return Optional Jwt if valid, empty otherwise
     */
    public Optional<Jwt> validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return Optional.of(jwt);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Extract jti (JWT ID) claim from a token.
     *
     * @param token the JWT token
     * @return Optional jti string
     */
    public Optional<String> extractJti(String token) {
        return validateToken(token)
                .map(jwt -> jwt.getClaimAsString("jti"));
    }

    /**
     * Extract expiry time from a token.
     *
     * @param token the JWT token
     * @return Optional expiry Instant
     */
    public Optional<Instant> extractExpiry(String token) {
        return validateToken(token)
                .map(Jwt::getExpiresAt);
    }

    /**
     * Extract username (subject) from a token.
     *
     * @param token the JWT token
     * @return Optional username
     */
    public Optional<String> extractUsername(String token) {
        return validateToken(token)
                .map(Jwt::getSubject);
    }
}

