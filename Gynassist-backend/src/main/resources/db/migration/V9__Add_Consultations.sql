-- Add Consultations Table
CREATE TABLE consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    scheduled_date_time TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    app_fee DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(50),
    payment_method VARCHAR(50),
    payment_transaction_id VARCHAR(255),
    payment_date_time TIMESTAMP,
    client_notes TEXT,
    provider_notes TEXT,
    meeting_link VARCHAR(500),
    meeting_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES healthcare_providers(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_consultation_client ON consultations(client_id);
CREATE INDEX idx_consultation_provider ON consultations(provider_id);
CREATE INDEX idx_consultation_status ON consultations(status);
CREATE INDEX idx_consultation_payment_status ON consultations(payment_status);
CREATE INDEX idx_consultation_scheduled_date ON consultations(scheduled_date_time);
CREATE INDEX idx_consultation_payment_date ON consultations(payment_date_time);