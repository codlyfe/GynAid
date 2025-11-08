-- V5: Add Advanced AI Features - Smart Matching and Analytics
-- This migration adds Phase 3B features while preserving all existing functionality

-- Provider Matches table for AI-powered provider recommendations
CREATE TABLE provider_matches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    match_score DECIMAL(3,2) NOT NULL,
    match_reason TEXT,
    match_type VARCHAR(30),
    distance_km DECIMAL(5,2),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_viewed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

-- Health Trends table for advanced analytics
CREATE TABLE health_trends (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    trend_type VARCHAR(30) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    current_value DECIMAL(10,4),
    previous_value DECIMAL(10,4),
    trend_direction VARCHAR(20),
    significance_score DECIMAL(3,2),
    analysis_period_days INTEGER,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX idx_provider_matches_user_id ON provider_matches(user_id);
CREATE INDEX idx_provider_matches_provider_id ON provider_matches(provider_id);
CREATE INDEX idx_provider_matches_score ON provider_matches(match_score DESC);
CREATE INDEX idx_provider_matches_generated_at ON provider_matches(generated_at);

CREATE INDEX idx_health_trends_user_id ON health_trends(user_id);
CREATE INDEX idx_health_trends_type ON health_trends(trend_type);
CREATE INDEX idx_health_trends_calculated_at ON health_trends(calculated_at);

-- Add constraints for data integrity
ALTER TABLE provider_matches ADD CONSTRAINT chk_match_score 
    CHECK (match_score >= 0 AND match_score <= 1);

ALTER TABLE provider_matches ADD CONSTRAINT chk_match_type 
    CHECK (match_type IN ('CONDITION_SPECIALIST', 'LOCATION_BASED', 'EXPERIENCE_MATCH', 
                         'EMERGENCY_AVAILABLE', 'GENERAL_CARE'));

ALTER TABLE health_trends ADD CONSTRAINT chk_trend_type 
    CHECK (trend_type IN ('CYCLE_REGULARITY', 'SYMPTOM_SEVERITY', 'MOOD_PATTERN', 
                         'PAIN_LEVEL', 'FLOW_INTENSITY', 'FERTILITY_INDICATOR'));

ALTER TABLE health_trends ADD CONSTRAINT chk_trend_direction 
    CHECK (trend_direction IN ('IMPROVING', 'STABLE', 'DECLINING', 'FLUCTUATING'));

ALTER TABLE health_trends ADD CONSTRAINT chk_significance_score 
    CHECK (significance_score IS NULL OR (significance_score >= 0 AND significance_score <= 1));