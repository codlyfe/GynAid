package com.gynaid.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise-grade Rate Limiting Service for GynAid
 * Implements token bucket algorithm for authentication endpoints
 * 
 * Security Features:
 * - Prevents brute force attacks on login/register endpoints
 * - Configurable rate limits per IP and user
 * - Uses Redis for distributed rate limiting
 * - Automatic cleanup of expired entries
 */
@Slf4j
@Service
public class RateLimitingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    
    // Rate limiting configuration
    private static final String LOGIN_RATE_LIMIT_KEY = "rate_limit:login:";
    private static final String REGISTER_RATE_LIMIT_KEY = "rate_limit:register:";
    private static final String API_RATE_LIMIT_KEY = "rate_limit:api:";
    
    // Configuration constants
    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final int LOGIN_WINDOW_MINUTES = 15;
    
    private static final int REGISTER_MAX_ATTEMPTS = 3;
    private static final int REGISTER_WINDOW_MINUTES = 60;
    
    private static final int API_MAX_REQUESTS = 100;
    private static final int API_WINDOW_MINUTES = 1;

    public RateLimitingService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Check if login attempt is allowed
     * @param clientIp IP address of the client
     * @param email Email of the user attempting login
     * @return RateLimitResult indicating if request is allowed
     */
    public RateLimitResult checkLoginRateLimit(String clientIp, String email) {
        String key = LOGIN_RATE_LIMIT_KEY + getKey(clientIp, email);
        return checkRateLimit(key, LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW_MINUTES, "login attempts");
    }

    /**
     * Check if registration attempt is allowed
     * @param clientIp IP address of the client
     * @param email Email of the user attempting registration
     * @return RateLimitResult indicating if request is allowed
     */
    public RateLimitResult checkRegisterRateLimit(String clientIp, String email) {
        String key = REGISTER_RATE_LIMIT_KEY + getKey(clientIp, email);
        return checkRateLimit(key, REGISTER_MAX_ATTEMPTS, REGISTER_WINDOW_MINUTES, "registration attempts");
    }

    /**
     * Check if API request is allowed
     * @param clientIp IP address of the client
     * @param userId User ID if authenticated
     * @return RateLimitResult indicating if request is allowed
     */
    public RateLimitResult checkApiRateLimit(String clientIp, Long userId) {
        String key = API_RATE_LIMIT_KEY + getKey(clientIp, userId != null ? userId.toString() : "anonymous");
        return checkRateLimit(key, API_MAX_REQUESTS, API_WINDOW_MINUTES, "API requests");
    }

    /**
     * Check rate limit using Redis atomic operations
     */
    private RateLimitResult checkRateLimit(String key, int maxAttempts, int windowMinutes, String operationType) {
        try {
            // Use Redis script for atomic rate limit check
            String luaScript = 
                "local current = redis.call('GET', KEYS[1]) " +
                "if current == false then " +
                "    redis.call('SETEX', KEYS[1], ARGV[1], 1) " +
                "    return {1, ARGV[2]} " +
                "else " +
                "    local count = tonumber(current) " +
                "    if count < tonumber(ARGV[2]) then " +
                "        redis.call('INCR', KEYS[1]) " +
                "        return {count + 1, ARGV[2]} " +
                "    else " +
                "        local ttl = redis.call('TTL', KEYS[1]) " +
                "        return {count, ttl} " +
                "    end " +
                "end";

            DefaultRedisScript<java.util.List> script = new DefaultRedisScript<>(luaScript, java.util.List.class);
            
            java.util.List<Long> result = redisTemplate.execute(script, 
                Collections.singletonList(key), 
                String.valueOf(windowMinutes * 60), // TTL in seconds
                String.valueOf(maxAttempts) // Max attempts
            );
            
            long currentCount = result.get(0);
            long remainingTime = result.get(1);
            
            boolean allowed = currentCount <= maxAttempts;
            
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {}, operation: {}, count: {}, remainingTime: {}s", 
                    maskKey(key), operationType, currentCount, remainingTime);
            }
            
            return RateLimitResult.builder()
                .allowed(allowed)
                .currentCount(currentCount)
                .maxAttempts(maxAttempts)
                .remainingAttempts(Math.max(0, maxAttempts - (int)currentCount))
                .remainingTimeSeconds(remainingTime)
                .resetTime(LocalDateTime.now().plusSeconds(remainingTime))
                .operationType(operationType)
                .build();
                
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if rate limiting service is down
            return RateLimitResult.builder()
                .allowed(true)
                .currentCount(0)
                .maxAttempts(maxAttempts)
                .remainingAttempts(maxAttempts)
                .remainingTimeSeconds(0L)
                .resetTime(LocalDateTime.now())
                .operationType(operationType)
                .error("Rate limiting service temporarily unavailable")
                .build();
        }
    }

    /**
     * Reset rate limit for specific key (admin use only)
     */
    public void resetRateLimit(String operationType, String clientIp, String identifier) {
        String key = getRateLimitKey(operationType, clientIp, identifier);
        redisTemplate.delete(key);
        log.info("Rate limit reset for key: {}", maskKey(key));
    }

    /**
     * Get current rate limit status for monitoring
     */
    public RateLimitStatus getRateLimitStatus(String operationType, String clientIp, String identifier) {
        String key = getRateLimitKey(operationType, clientIp, identifier);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return RateLimitStatus.builder()
                .key(maskKey(key))
                .currentCount(0)
                .maxAttempts(getMaxAttempts(operationType))
                .windowMinutes(getWindowMinutes(operationType))
                .build();
        }
        
        Integer count = Integer.valueOf(value.toString());
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        return RateLimitStatus.builder()
            .key(maskKey(key))
            .currentCount(count)
            .maxAttempts(getMaxAttempts(operationType))
            .windowMinutes(getWindowMinutes(operationType))
            .remainingTimeSeconds(ttl)
            .build();
    }

    private String getKey(String clientIp, String identifier) {
        return clientIp + ":" + identifier;
    }

    private String getRateLimitKey(String operationType, String clientIp, String identifier) {
        return switch (operationType.toLowerCase()) {
            case "login" -> LOGIN_RATE_LIMIT_KEY + getKey(clientIp, identifier);
            case "register" -> REGISTER_RATE_LIMIT_KEY + getKey(clientIp, identifier);
            case "api" -> API_RATE_LIMIT_KEY + getKey(clientIp, identifier);
            default -> API_RATE_LIMIT_KEY + getKey(clientIp, identifier);
        };
    }

    private int getMaxAttempts(String operationType) {
        return switch (operationType.toLowerCase()) {
            case "login" -> LOGIN_MAX_ATTEMPTS;
            case "register" -> REGISTER_MAX_ATTEMPTS;
            case "api" -> API_MAX_REQUESTS;
            default -> API_MAX_REQUESTS;
        };
    }

    private int getWindowMinutes(String operationType) {
        return switch (operationType.toLowerCase()) {
            case "login" -> LOGIN_WINDOW_MINUTES;
            case "register" -> REGISTER_WINDOW_MINUTES;
            case "api" -> API_WINDOW_MINUTES;
            default -> API_WINDOW_MINUTES;
        };
    }

    /**
     * Mask sensitive parts of keys for logging
     */
    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        
        // Mask email part if present
        if (key.contains("@")) {
            String[] parts = key.split(":");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("@")) {
                    parts[i] = parts[i].substring(0, 2) + "***@" + "***";
                }
            }
            return String.join(":", parts);
        }
        
        // Mask IP if present
        if (key.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            return key.replaceAll("(\\d+\\.\\d+\\.\\d+\\.)\\d+", "$1***");
        }
        
        return "***";
    }

    // Data classes for results
    public static class RateLimitResult {
        private boolean allowed;
        private long currentCount;
        private int maxAttempts;
        private int remainingAttempts;
        private long remainingTimeSeconds;
        private LocalDateTime resetTime;
        private String operationType;
        private String error;

        // Getters
        public boolean isAllowed() { return allowed; }
        public long getCurrentCount() { return currentCount; }
        public int getMaxAttempts() { return maxAttempts; }
        public int getRemainingAttempts() { return remainingAttempts; }
        public long getRemainingTimeSeconds() { return remainingTimeSeconds; }
        public LocalDateTime getResetTime() { return resetTime; }
        public String getOperationType() { return operationType; }
        public String getError() { return error; }

        public static class Builder {
            private RateLimitResult result = new RateLimitResult();

            public Builder allowed(boolean allowed) {
                result.allowed = allowed;
                return this;
            }

            public Builder currentCount(long currentCount) {
                result.currentCount = currentCount;
                return this;
            }

            public Builder maxAttempts(int maxAttempts) {
                result.maxAttempts = maxAttempts;
                return this;
            }

            public Builder remainingAttempts(int remainingAttempts) {
                result.remainingAttempts = remainingAttempts;
                return this;
            }

            public Builder remainingTimeSeconds(long remainingTimeSeconds) {
                result.remainingTimeSeconds = remainingTimeSeconds;
                return this;
            }

            public Builder resetTime(LocalDateTime resetTime) {
                result.resetTime = resetTime;
                return this;
            }

            public Builder operationType(String operationType) {
                result.operationType = operationType;
                return this;
            }

            public Builder error(String error) {
                result.error = error;
                return this;
            }

            public RateLimitResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public static class RateLimitStatus {
        private String key;
        private int currentCount;
        private int maxAttempts;
        private int windowMinutes;
        private long remainingTimeSeconds;

        // Getters
        public String getKey() { return key; }
        public int getCurrentCount() { return currentCount; }
        public int getMaxAttempts() { return maxAttempts; }
        public int getWindowMinutes() { return windowMinutes; }
        public long getRemainingTimeSeconds() { return remainingTimeSeconds; }

        public static class Builder {
            private RateLimitStatus result = new RateLimitStatus();

            public Builder key(String key) {
                result.key = key;
                return this;
            }

            public Builder currentCount(int currentCount) {
                result.currentCount = currentCount;
                return this;
            }

            public Builder maxAttempts(int maxAttempts) {
                result.maxAttempts = maxAttempts;
                return this;
            }

            public Builder windowMinutes(int windowMinutes) {
                result.windowMinutes = windowMinutes;
                return this;
            }

            public Builder remainingTimeSeconds(long remainingTimeSeconds) {
                result.remainingTimeSeconds = remainingTimeSeconds;
                return this;
            }

            public RateLimitStatus build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}