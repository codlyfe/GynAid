-- V6: Add UX Enhancement Features - Voice Integration and Smart Notifications
-- This migration adds Phase 3C features while preserving all existing functionality

-- Voice Interactions table for hands-free health tracking
CREATE TABLE voice_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    audio_transcript TEXT,
    ai_response TEXT,
    interaction_type VARCHAR(30),
    language_code VARCHAR(5) DEFAULT 'EN',
    confidence_score DECIMAL(3,2),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Smart Notifications table for predictive health alerts
CREATE TABLE smart_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_sent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX idx_voice_interactions_user_id ON voice_interactions(user_id);
CREATE INDEX idx_voice_interactions_type ON voice_interactions(interaction_type);
CREATE INDEX idx_voice_interactions_processed_at ON voice_interactions(processed_at);

CREATE INDEX idx_smart_notifications_user_id ON smart_notifications(user_id);
CREATE INDEX idx_smart_notifications_type ON smart_notifications(notification_type);
CREATE INDEX idx_smart_notifications_scheduled_for ON smart_notifications(scheduled_for);
CREATE INDEX idx_smart_notifications_priority ON smart_notifications(priority);
CREATE INDEX idx_smart_notifications_is_sent ON smart_notifications(is_sent);

-- Add constraints for data integrity
ALTER TABLE voice_interactions ADD CONSTRAINT chk_interaction_type 
    CHECK (interaction_type IN ('SYMPTOM_LOGGING', 'CYCLE_TRACKING', 'HEALTH_QUERY', 
                               'EMERGENCY_REQUEST', 'APPOINTMENT_BOOKING'));

ALTER TABLE voice_interactions ADD CONSTRAINT chk_language_code 
    CHECK (language_code IN ('EN', 'LG', 'SW'));

ALTER TABLE voice_interactions ADD CONSTRAINT chk_voice_confidence_score 
    CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 1));

ALTER TABLE smart_notifications ADD CONSTRAINT chk_notification_type 
    CHECK (notification_type IN ('PERIOD_REMINDER', 'FERTILITY_WINDOW', 'MEDICATION_REMINDER',
                                'APPOINTMENT_REMINDER', 'HEALTH_TIP', 'EMERGENCY_ALERT', 'MOH_UPDATE'));

ALTER TABLE smart_notifications ADD CONSTRAINT chk_smart_notification_priority 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
