-- Enable PostGIS for spatial data (for PostgreSQL)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address TEXT,
    city VARCHAR(100),
    district VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Uganda',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Provider locations table with spatial data
CREATE TABLE IF NOT EXISTS provider_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    current_location geometry(Point, 4326),
    availability_status VARCHAR(50) DEFAULT 'OFFLINE',
    current_activity VARCHAR(255),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accuracy DOUBLE PRECISION,
    service_type VARCHAR(50),
    FOREIGN KEY (provider_id) REFERENCES users(id)
);

-- MOH Licenses table
CREATE TABLE IF NOT EXISTS moh_licenses (
    license_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    specialization VARCHAR(50),
    issuing_authority VARCHAR(100),
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    last_verified DATE,
    verification_remarks TEXT,
    document_url TEXT,
    FOREIGN KEY (provider_id) REFERENCES users(id)
);

-- Insurance policies table
CREATE TABLE IF NOT EXISTS insurance_policies (
    policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    insurance_provider VARCHAR(100),
    policy_number VARCHAR(100),
    group_number VARCHAR(100),
    effective_date DATE,
    expiry_date DATE,
    coverage_details TEXT,
    coverage_limit DOUBLE PRECISION,
    deductible DOUBLE PRECISION,
    status VARCHAR(50) DEFAULT 'PENDING',
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    card_image_url TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- CREATE INDEX IF NOT EXISTS idx_provider_locations_location ON provider_locations USING GIST (current_location);
CREATE INDEX IF NOT EXISTS idx_provider_locations_status ON provider_locations(availability_status);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);