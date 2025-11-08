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
@Table(name = "moh_licenses")
public class MOHLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long licenseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User user;
    
    @Column(unique = true, nullable = false)
    private String licenseNumber;
    
    private LocalDate issueDate;
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    private LicenseSpecialization specialization;
    
    private String issuingAuthority;
    
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
    
    private LocalDate lastVerified;
    private String verificationRemarks;
    private String documentUrl;
    
    public enum VerificationStatus {
        PENDING, VERIFIED, EXPIRED, REVOKED, SUSPENDED
    }
    
    public enum LicenseSpecialization {
        GYNECOLOGY, OBSTETRICS, FERTILITY, GENERAL_PRACTICE, MIDWIFERY
    }
}