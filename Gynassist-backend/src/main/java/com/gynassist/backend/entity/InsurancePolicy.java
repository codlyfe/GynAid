package com.gynassist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_policies")
public class InsurancePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String insuranceProvider;
    private String policyNumber;
    private String groupNumber;
    
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    
    private String coverageDetails;
    private Double coverageLimit;
    private Double deductible;
    
    @Enumerated(EnumType.STRING)
    private PolicyStatus status;
    
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
    
    private String cardImageUrl;
    
    public enum PolicyStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING
    }
    
    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}