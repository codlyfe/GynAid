-- Migration V3: Add Gynecological Profiles and Cycle Tracking
-- Extends health profiles with comprehensive reproductive health data

-- Create gynecological_profiles table
CREATE TABLE IF NOT EXISTS gynecological_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    health_profile_id BIGINT NOT NULL,
    age_at_first_period INTEGER,
    average_cycle_length INTEGER,
    last_period_date DATE,
    cycle_regularity VARCHAR(50),
    flow_intensity VARCHAR(50),
    pregnancies_count INTEGER DEFAULT 0,
    live_births_count INTEGER DEFAULT 0,
    miscarriages_count INTEGER DEFAULT 0,
    contraception_method VARCHAR(100),
    fertility_goal VARCHAR(100),
    trying_to_conceive_months INTEGER,
    smoking_status VARCHAR(50),
    alcohol_consumption VARCHAR(50),
    exercise_frequency VARCHAR(50),
    stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gynecological_profile_health FOREIGN KEY (health_profile_id) REFERENCES client_health_profiles(id) ON DELETE CASCADE,
    CONSTRAINT unique_health_profile_gynecological UNIQUE (health_profile_id)
);

-- Create menstruation_cycles table
CREATE TABLE IF NOT EXISTS menstruation_cycles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gynecological_profile_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    cycle_length INTEGER,
    flow_intensity VARCHAR(50),
    symptoms TEXT,
    mood_notes TEXT,
    pain_level INTEGER CHECK (pain_level >= 0 AND pain_level <= 10),
    notes TEXT,
    is_predicted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menstruation_cycle_gynecological FOREIGN KEY (gynecological_profile_id) REFERENCES gynecological_profiles(id) ON DELETE CASCADE
);

-- Create medical_histories table
CREATE TABLE IF NOT EXISTS medical_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    health_profile_id BIGINT NOT NULL,
    chronic_conditions TEXT,
    allergies TEXT,
    current_medications TEXT,
    family_history TEXT,
    previous_surgeries TEXT,
    reproductive_health_issues TEXT,
    hiv_status VARCHAR(50),
    last_pap_smear_date VARCHAR(100),
    last_mammogram_date VARCHAR(100),
    vaccination_history TEXT,
    disclosure_preference VARCHAR(50) DEFAULT 'PRIVATE',
    additional_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_history_health FOREIGN KEY (health_profile_id) REFERENCES client_health_profiles(id) ON DELETE CASCADE,
    CONSTRAINT unique_health_profile_medical UNIQUE (health_profile_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_gynecological_profiles_health_id ON gynecological_profiles(health_profile_id);
CREATE INDEX IF NOT EXISTS idx_menstruation_cycles_gynecological_id ON menstruation_cycles(gynecological_profile_id);
CREATE INDEX IF NOT EXISTS idx_menstruation_cycles_start_date ON menstruation_cycles(start_date);
CREATE INDEX IF NOT EXISTS idx_medical_histories_health_id ON medical_histories(health_profile_id);

-- Add comments for documentation
COMMENT ON TABLE gynecological_profiles IS 'Comprehensive reproductive health profiles for female clients';
COMMENT ON TABLE menstruation_cycles IS 'Individual menstrual cycle tracking entries';
COMMENT ON TABLE medical_histories IS 'Detailed medical history and health background information';
