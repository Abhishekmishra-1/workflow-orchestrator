package com.authservice.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed rate limiting service.
 * Handles rate limiting for authentication endpoints.
 */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final int LOGIN_MAX_REQUESTS = 10;
    private static final int LOGIN_WINDOW_SECONDS = 60;
    private static final int REFRESH_MAX_REQUESTS = 50;
    private static final int REFRESH_WINDOW_SECONDS = 86400; // 24 hours

    /**
     * Check if login request is allowed for username/IP.
     * Limit: 10 requests per minute per username or IP.
     *
     * @param username the username
     * @param ipAddress the IP address
     * @return true if allowed, false if rate limited
     */
    public boolean isLoginAllowed(String username, String ipAddress) {
        try {
            String usernameKey = RATE_LIMIT_PREFIX + "login:user:" + username;
            String ipKey = RATE_LIMIT_PREFIX + "login:ip:" + ipAddress;

            long usernameCount = incrementAndGet(usernameKey, LOGIN_WINDOW_SECONDS);
            long ipCount = incrementAndGet(ipKey, LOGIN_WINDOW_SECONDS);

            boolean allowed = usernameCount <= LOGIN_MAX_REQUESTS && ipCount <= LOGIN_MAX_REQUESTS;

            if (!allowed) {
                log.warn("Login rate limit exceeded for username={} or ip={}", username, ipAddress);
            }

            return allowed;
        } catch (Exception e) {
            log.error("Error checking login rate limit, allowing request", e);
            return true; // Fail open
        }
    }

    /**
     * Check if refresh request is allowed for username/IP.
     * Limit: 50 requests per day per username or IP.
     *
     * @param username the username
     * @param ipAddress the IP address
     * @return true if allowed, false if rate limited
     */
    public boolean isRefreshAllowed(String username, String ipAddress) {
        try {
            String usernameKey = RATE_LIMIT_PREFIX + "refresh:user:" + username;
            String ipKey = RATE_LIMIT_PREFIX + "refresh:ip:" + ipAddress;

            long usernameCount = incrementAndGet(usernameKey, REFRESH_WINDOW_SECONDS);
            long ipCount = incrementAndGet(ipKey, REFRESH_WINDOW_SECONDS);

            boolean allowed = usernameCount <= REFRESH_MAX_REQUESTS && ipCount <= REFRESH_MAX_REQUESTS;

            if (!allowed) {
                log.warn("Refresh rate limit exceeded for username={} or ip={}", username, ipAddress);
            }

            return allowed;
        } catch (Exception e) {
            log.error("Error checking refresh rate limit, allowing request", e);
            return true; // Fail open
        }
    }

    /**
     * Check if revoke request is allowed for username/IP.
     * Uses same limits as refresh (50/day).
     *
     * @param username the username
     * @param ipAddress the IP address
     * @return true if allowed, false if rate limited
     */
    public boolean isRevokeAllowed(String username, String ipAddress) {
        try {
            String usernameKey = RATE_LIMIT_PREFIX + "revoke:user:" + username;
            String ipKey = RATE_LIMIT_PREFIX + "revoke:ip:" + ipAddress;

            long usernameCount = incrementAndGet(usernameKey, REFRESH_WINDOW_SECONDS);
            long ipCount = incrementAndGet(ipKey, REFRESH_WINDOW_SECONDS);

            boolean allowed = usernameCount <= REFRESH_MAX_REQUESTS && ipCount <= REFRESH_MAX_REQUESTS;

            if (!allowed) {
                log.warn("Revoke rate limit exceeded for username={} or ip={}", username, ipAddress);
            }

            return allowed;
        } catch (Exception e) {
            log.error("Error checking revoke rate limit, allowing request", e);
            return true; // Fail open
        }
    }

    private long incrementAndGet(String key, int ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return 0;
        }
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }
        return count;
    }
}

