package com.authservice.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Service for managing token revocation using Redis.
 * Stores revoked JWT IDs (jti) with TTL matching token expiry.
 * Fails gracefully if Redis is unavailable - allows token validation but logs warnings.
 */
@Service
@RequiredArgsConstructor
public class RedisRevocationService {

    private static final Logger log = LoggerFactory.getLogger(RedisRevocationService.class);
    private static final String REVOKED_KEY_PREFIX = "revoked:jti:";
    private final StringRedisTemplate redisTemplate;

    /**
     * Revoke a token by its JWT ID (jti).
     * Stores the jti in Redis with TTL equal to token expiry.
     * Fails gracefully if Redis is unavailable.
     *
     * @param jti the JWT ID
     * @param expiryEpochMillis the token expiry time in epoch milliseconds
     */
    public void revokeTokenJti(String jti, long expiryEpochMillis) {
        try {
            String key = REVOKED_KEY_PREFIX + jti;
            long now = Instant.now().toEpochMilli();
            long ttlSeconds = Math.max(1, (expiryEpochMillis - now - 5000) / 1000);
            
            redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Failed to revoke token in Redis (Redis may be unavailable). Token jti={} will remain valid until expiry. Error: {}", jti, e.getMessage());
            // Fail gracefully - token will remain valid until natural expiry
        }
    }

    /**
     * Check if a token is revoked by its JWT ID (jti).
     * Returns false if Redis is unavailable (fail open for availability).
     *
     * @param jti the JWT ID
     * @return true if the token is revoked, false otherwise (or if Redis unavailable)
     */
    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            String key = REVOKED_KEY_PREFIX + jti;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Failed to check token revocation in Redis (Redis may be unavailable). Allowing token validation. jti={}, error: {}", jti, e.getMessage());
            // Fail open - allow token validation if Redis is down
            return false;
        }
    }

    /**
     * Remove a revocation entry (useful for testing or manual cleanup).
     *
     * @param jti the JWT ID
     */
    public void removeRevocation(String jti) {
        try {
            String key = REVOKED_KEY_PREFIX + jti;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to remove revocation entry from Redis. jti={}, error: {}", jti, e.getMessage());
        }
    }
}

