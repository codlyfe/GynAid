-- V4: Add AI Features - Health Insights and Symptom Analysis
-- This migration adds AI-powered features while preserving all existing functionality

-- Health Insights table for AI-generated personalized recommendations
CREATE TABLE health_insights (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    insight_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    confidence_score DECIMAL(3,2),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged BOOLEAN DEFAULT FALSE,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Symptom Analysis table for AI-powered health assessment
CREATE TABLE symptom_analyses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symptoms TEXT NOT NULL,
    ai_analysis TEXT,
    risk_level VARCHAR(20),
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    requires_provider_attention BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Symptom Recommendations table (related to symptom_analyses)
CREATE TABLE symptom_recommendations (
    analysis_id BIGINT NOT NULL,
    recommendation TEXT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES symptom_analyses(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX idx_health_insights_user_id ON health_insights(user_id);
CREATE INDEX idx_health_insights_type ON health_insights(insight_type);
CREATE INDEX idx_health_insights_priority ON health_insights(priority);
CREATE INDEX idx_health_insights_generated_at ON health_insights(generated_at);

CREATE INDEX idx_symptom_analyses_user_id ON symptom_analyses(user_id);
CREATE INDEX idx_symptom_analyses_risk_level ON symptom_analyses(risk_level);
CREATE INDEX idx_symptom_analyses_analyzed_at ON symptom_analyses(analyzed_at);

-- Add constraints for data integrity
ALTER TABLE health_insights ADD CONSTRAINT chk_insight_type 
    CHECK (insight_type IN ('CYCLE_PREDICTION', 'FERTILITY_WINDOW', 'HEALTH_RISK', 
                           'LIFESTYLE_RECOMMENDATION', 'PROVIDER_SUGGESTION', 'EMERGENCY_ALERT'));

ALTER TABLE health_insights ADD CONSTRAINT chk_priority 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

ALTER TABLE health_insights ADD CONSTRAINT chk_confidence_score 
    CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 1));

ALTER TABLE symptom_analyses ADD CONSTRAINT chk_risk_level 
    CHECK (risk_level IN ('LOW', 'MODERATE', 'HIGH', 'EMERGENCY'));
