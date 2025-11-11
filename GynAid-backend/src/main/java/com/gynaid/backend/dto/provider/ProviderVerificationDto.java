package com.gynaid.backend.dto.provider;

import com.gynaid.backend.entity.provider.ProviderVerification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for provider verification information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderVerificationDto {
    private Long id;
    private Long providerId;
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;
    private ProviderVerification.Specialization specialization;
    private String qualifications;
    private Integer yearsOfExperience;
    private String bio;
    private ProviderVerification.VerificationStatus verificationStatus;
    private String verificationNotes;
    private LocalDateTime verifiedAt;
}


