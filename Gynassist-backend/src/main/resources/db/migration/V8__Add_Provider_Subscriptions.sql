-- Add Provider Subscriptions Table
CREATE TABLE provider_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    plan VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    monthly_fee DECIMAL(10,2) NOT NULL,
    total_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_payment_date TIMESTAMP,
    next_billing_date TIMESTAMP,
    
    -- Commercial features
    priority_ranking INTEGER DEFAULT 1,
    featured_listing BOOLEAN DEFAULT FALSE,
    premium_badge BOOLEAN DEFAULT FALSE,
    max_photos INTEGER DEFAULT 3,
    video_consultation_enabled BOOLEAN DEFAULT FALSE,
    emergency_calls_enabled BOOLEAN DEFAULT FALSE,
    analytics_access BOOLEAN DEFAULT FALSE,
    custom_branding BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (provider_id) REFERENCES healthcare_providers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_provider_subscription (provider_id)
);

-- Add indexes for performance
CREATE INDEX idx_subscription_status ON provider_subscriptions(status);
CREATE INDEX idx_subscription_end_date ON provider_subscriptions(end_date);
CREATE INDEX idx_subscription_billing_date ON provider_subscriptions(next_billing_date);
CREATE INDEX idx_subscription_plan ON provider_subscriptions(plan);

-- Add sample subscription data for existing providers
INSERT INTO provider_subscriptions (provider_id, plan, status, start_date, end_date, monthly_fee, total_paid, priority_ranking, featured_listing, premium_badge, max_photos, video_consultation_enabled)
SELECT 
    id,
    CASE 
        WHEN name LIKE '%Dr.%' THEN 'PROFESSIONAL'
        WHEN type = 'HOSPITAL' THEN 'PREMIUM'
        WHEN type = 'SPECIALIST_CENTER' THEN 'PREMIUM'
        ELSE 'BASIC'
    END as plan,
    'ACTIVE' as status,
    CURRENT_TIMESTAMP as start_date,
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 12 MONTH) as end_date,
    CASE 
        WHEN name LIKE '%Dr.%' THEN 150000.00
        WHEN type = 'HOSPITAL' THEN 300000.00
        WHEN type = 'SPECIALIST_CENTER' THEN 300000.00
        ELSE 50000.00
    END as monthly_fee,
    CASE 
        WHEN name LIKE '%Dr.%' THEN 150000.00
        WHEN type = 'HOSPITAL' THEN 300000.00
        WHEN type = 'SPECIALIST_CENTER' THEN 300000.00
        ELSE 50000.00
    END as total_paid,
    CASE 
        WHEN name LIKE '%Dr.%' THEN 3
        WHEN type = 'HOSPITAL' THEN 5
        WHEN type = 'SPECIALIST_CENTER' THEN 5
        ELSE 1
    END as priority_ranking,
    CASE 
        WHEN type IN ('HOSPITAL', 'SPECIALIST_CENTER') THEN TRUE
        ELSE FALSE
    END as featured_listing,
    CASE 
        WHEN name LIKE '%Dr.%' OR type IN ('HOSPITAL', 'SPECIALIST_CENTER') THEN TRUE
        ELSE FALSE
    END as premium_badge,
    CASE 
        WHEN name LIKE '%Dr.%' THEN 10
        WHEN type IN ('HOSPITAL', 'SPECIALIST_CENTER') THEN 25
        ELSE 3
    END as max_photos,
    CASE 
        WHEN name LIKE '%Dr.%' OR type IN ('HOSPITAL', 'SPECIALIST_CENTER') THEN TRUE
        ELSE FALSE
    END as video_consultation_enabled
FROM healthcare_providers 
WHERE verification_status = 'VERIFIED';