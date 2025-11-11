-- Migration V2: Add Client Health Profiles (Non-Breaking)
-- This migration only adds new tables and does not modify existing ones

-- Create client_health_profiles table
CREATE TABLE IF NOT EXISTS client_health_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    emergency_contact_relationship VARCHAR(100),
    profile_completion_percentage INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_client_health_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_health_profile UNIQUE (user_id)
);

-- Create medical_vitals table
CREATE TABLE IF NOT EXISTS medical_vitals (
    id BIGSERIAL PRIMARY KEY,
    health_profile_id BIGINT NOT NULL,
    height_cm DECIMAL(5,2),
    weight_kg DECIMAL(5,2),
    blood_pressure_systolic INTEGER,
    blood_pressure_diastolic INTEGER,
    blood_type VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_vitals_profile FOREIGN KEY (health_profile_id) REFERENCES client_health_profiles(id) ON DELETE CASCADE,
    CONSTRAINT unique_profile_vitals UNIQUE (health_profile_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_client_health_profiles_user_id ON client_health_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_medical_vitals_profile_id ON medical_vitals(health_profile_id);
