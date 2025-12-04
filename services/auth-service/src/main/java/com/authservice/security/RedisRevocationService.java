package com.authservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Service for managing token revocation using Redis.
 * Stores revoked JWT IDs (jti) with TTL matching token expiry.
 */
@Service
@RequiredArgsConstructor
public class RedisRevocationService {

    private static final String REVOKED_KEY_PREFIX = "revoked:jti:";
    private final StringRedisTemplate redisTemplate;

    /**
     * Revoke a token by its JWT ID (jti).
     * Stores the jti in Redis with TTL equal to token expiry.
     *
     * @param jti the JWT ID
     * @param expiryEpochMillis the token expiry time in epoch milliseconds
     */
    public void revokeTokenJti(String jti, long expiryEpochMillis) {
        String key = REVOKED_KEY_PREFIX + jti;
        long now = Instant.now().toEpochMilli();
        long ttlSeconds = Math.max(1, (expiryEpochMillis - now) / 1000);
        
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Check if a token is revoked by its JWT ID (jti).
     *
     * @param jti the JWT ID
     * @return true if the token is revoked, false otherwise
     */
    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        String key = REVOKED_KEY_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Remove a revocation entry (useful for testing or manual cleanup).
     *
     * @param jti the JWT ID
     */
    public void removeRevocation(String jti) {
        String key = REVOKED_KEY_PREFIX + jti;
        redisTemplate.delete(key);
    }
}

