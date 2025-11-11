-- Create healthcare_providers table if it doesn't exist
CREATE TABLE IF NOT EXISTS healthcare_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    license_number VARCHAR(100),
    specialization VARCHAR(100),
    years_of_experience INTEGER,
    clinic_name VARCHAR(255),
    clinic_address TEXT,
    consultation_fee DECIMAL(10,2),
    is_verified BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

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
    CONSTRAINT unique_provider_subscription UNIQUE (provider_id)
);