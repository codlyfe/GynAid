-- Migration V1: Create Base Schema
-- Creates the foundational tables for Gynassist application

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    user_status VARCHAR(50) DEFAULT 'ACTIVE',
    profile_completion_status VARCHAR(50) DEFAULT 'INCOMPLETE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create providers table
CREATE TABLE providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_provider_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create provider_locations table
CREATE TABLE provider_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    provider_name VARCHAR(255),
    provider_email VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    availability_status VARCHAR(50) DEFAULT 'OFFLINE',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    service_type VARCHAR(100),
    current_activity VARCHAR(255),
    accuracy DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_provider_location_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

-- Create moh_licenses table
CREATE TABLE moh_licenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_number VARCHAR(255) NOT NULL UNIQUE,
    provider_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(100),
    issue_date DATE,
    expiry_date DATE,
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create insurance_policies table
CREATE TABLE insurance_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    policy_number VARCHAR(255) NOT NULL,
    provider_name VARCHAR(255) NOT NULL,
    policy_type VARCHAR(100),
    coverage_amount DECIMAL(15,2),
    premium_amount DECIMAL(10,2),
    start_date DATE,
    end_date DATE,
    policy_status VARCHAR(50) DEFAULT 'ACTIVE',
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_insurance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_providers_user_id ON providers(user_id);
CREATE INDEX idx_provider_locations_provider_id ON provider_locations(provider_id);
CREATE INDEX idx_provider_locations_coordinates ON provider_locations(latitude, longitude);
CREATE INDEX idx_moh_licenses_number ON moh_licenses(license_number);
CREATE INDEX idx_insurance_policies_user_id ON insurance_policies(user_id);