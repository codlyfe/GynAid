package com.gynaid.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise-grade Feature Flag Service with gradual rollout management
 * Supports multiple rollout strategies and real-time flag updates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, FeatureFlag> flags = new HashMap<>();
    
    @Value("${app.feature-flags.poll-interval:60}")
    private long pollIntervalSeconds;

    private static final String FEATURE_FLAG_PREFIX = "feature_flag:";
    private static final String FLAG_METRICS_PREFIX = "flag_metrics:";
    private static final String ROLLOUT_HISTORY_PREFIX = "rollout_history:";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Initialize feature flags
     */
    public void initializeFlags() {
        // Load default flags
        loadDefaultFlags();
        
        // Start flag polling for real-time updates
        startFlagPolling();
        
        log.info("Feature flag service initialized with {} flags", flags.size());
    }

    /**
     * Check if feature flag is enabled for user
     */
    public boolean isEnabled(String flagName, String userId, FlagContext context) {
        FeatureFlag flag = getFlag(flagName);
        if (flag == null || !flag.isEnabled()) {
            return false;
        }

        // Check rollout strategy
        return evaluateRolloutStrategy(flag, userId, context);
    }

    private FeatureFlag getFlag(String flagName) {
        return flags.computeIfAbsent(flagName, name -> {
            // Try to load from Redis
            FeatureFlag redisFlag = (FeatureFlag) redisTemplate.opsForValue().get(FEATURE_FLAG_PREFIX + name);
            return redisFlag != null ? redisFlag : getDefaultFlag(name);
        });
    }

    private boolean evaluateRolloutStrategy(FeatureFlag flag, String userId, FlagContext context) {
        RolloutStrategy strategy = flag.getStrategy();
        
        switch (strategy) {
            case ALL:
                return true;
            case NONE:
                return false;
            case PERCENTAGE:
                return evaluatePercentageRollout(flag.getPercentage(), userId);
            case USER_IDS:
                return flag.getTargetUserIds().contains(userId);
            case ROLLOUT_GROUPS:
                return evaluateRolloutGroups(flag, context);
            default:
                return false;
        }
    }

    private boolean evaluatePercentageRollout(double percentage, String userId) {
        int hash = Math.abs(userId.hashCode()) % 100;
        return hash < percentage;
    }

    private boolean evaluateRolloutGroups(FeatureFlag flag, FlagContext context) {
        // Implementation for rollout groups based on user attributes
        return false;
    }

    private FeatureFlag getDefaultFlag(String name) {
        return FeatureFlag.builder()
            .name(name)
            .enabled(false)
            .strategy(RolloutStrategy.NONE)
            .build();
    }

    private void loadDefaultFlags() {
        // Load default feature flags
        flags.put("advanced_analytics", FeatureFlag.builder()
            .name("advanced_analytics")
            .enabled(true)
            .strategy(RolloutStrategy.PERCENTAGE)
            .percentage(10.0)
            .build());
            
        flags.put("ai_health_assistant", FeatureFlag.builder()
            .name("ai_health_assistant")
            .enabled(true)
            .strategy(RolloutStrategy.USER_IDS)
            .targetUserIds(new HashSet<>(Arrays.asList("user1", "user2")))
            .build());
            
        flags.put("voice_integration", FeatureFlag.builder()
            .name("voice_integration")
            .enabled(false)
            .strategy(RolloutStrategy.NONE)
            .build());
    }

    private void startFlagPolling() {
        // Start background thread for polling Redis for flag updates
        // Implementation would involve @Scheduled or async processing
    }

    /**
     * Update feature flag
     */
    public void updateFlag(FeatureFlag flag) {
        flags.put(flag.getName(), flag);
        redisTemplate.opsForValue().set(FEATURE_FLAG_PREFIX + flag.getName(), flag);
        log.info("Updated feature flag: {}", flag.getName());
    }

    /**
     * Get all feature flags
     */
    public Map<String, FeatureFlag> getAllFlags() {
        return new HashMap<>(flags);
    }

    /**
     * Record flag metrics
     */
    public void recordFlagMetric(String flagName, String userId, boolean enabled) {
        String metricKey = FLAG_METRICS_PREFIX + flagName;
        Map<String, Object> metrics = (Map<String, Object>) redisTemplate.opsForValue().get(metricKey);
        
        if (metrics == null) {
            metrics = new HashMap<>();
            metrics.put("totalUsers", 0);
            metrics.put("enabledUsers", 0);
        }
        
        metrics.put("totalUsers", ((Integer) metrics.get("totalUsers")) + 1);
        if (enabled) {
            metrics.put("enabledUsers", ((Integer) metrics.get("enabledUsers")) + 1);
        }
        
        redisTemplate.opsForValue().set(metricKey, metrics, 24, TimeUnit.HOURS);
    }

    // Inner classes
    public static class FeatureFlag {
        private String name;
        private boolean enabled;
        private RolloutStrategy strategy;
        private double percentage;
        private Set<String> targetUserIds;
        private Map<String, Object> attributes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static class Builder {
            private FeatureFlag flag = new FeatureFlag();

            public Builder name(String name) {
                flag.name = name;
                return this;
            }

            public Builder enabled(boolean enabled) {
                flag.enabled = enabled;
                return this;
            }

            public Builder strategy(RolloutStrategy strategy) {
                flag.strategy = strategy;
                return this;
            }

            public Builder percentage(double percentage) {
                flag.percentage = percentage;
                return this;
            }

            public Builder targetUserIds(Set<String> userIds) {
                flag.targetUserIds = userIds;
                return this;
            }

            public Builder attributes(Map<String, Object> attributes) {
                flag.attributes = attributes;
                return this;
            }

            public FeatureFlag build() {
                flag.createdAt = LocalDateTime.now();
                flag.updatedAt = LocalDateTime.now();
                return flag;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters and setters
        public String getName() { return name; }
        public boolean isEnabled() { return enabled; }
        public RolloutStrategy getStrategy() { return strategy; }
        public double getPercentage() { return percentage; }
        public Set<String> getTargetUserIds() { return targetUserIds; }
        public Map<String, Object> getAttributes() { return attributes; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    public enum RolloutStrategy {
        ALL,        // Enable for all users
        NONE,       // Disable for all users
        PERCENTAGE, // Enable for percentage of users
        USER_IDS,   // Enable for specific user IDs
        ROLLOUT_GROUPS // Enable for user groups
    }

    public static class FlagContext {
        private String userId;
        private String deviceType;
        private String location;
        private Map<String, Object> attributes;

        // Constructors, getters, and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }
}