package com.gynaid.backend.entity.provider;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider verification and credential management.
 * Linked to User entity for providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provider_verifications")
public class ProviderVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    private User provider;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "specialization")
    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications; // JSON array of qualifications

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "document_urls", columnDefinition = "TEXT")
    private String documentUrls; // JSON array of document URLs

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Specialization {
        GYNECOLOGY,
        OBSTETRICS,
        GYNECOLOGICAL_ONCOLOGY,
        REPRODUCTIVE_ENDOCRINOLOGY,
        FERTILITY_SPECIALIST,
        MIDWIFERY,
        GENERAL_PRACTICE,
        OTHER
    }

    public enum VerificationStatus {
        PENDING,
        UNDER_REVIEW,
        VERIFIED,
        REJECTED,
        EXPIRED,
        SUSPENDED
    }

    // Add missing method for compatibility
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
}


