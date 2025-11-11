package com.gynaid.backend.service;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.provider.ProviderVerification;
import com.gynaid.backend.repository.provider.ProviderVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderVerificationService {
    
    private final ProviderVerificationRepository verificationRepository;
    
    public Optional<ProviderVerification> getVerification(Long providerId) {
        return verificationRepository.findByProviderId(providerId);
    }
    
    public ProviderVerification createOrUpdateVerification(User provider, ProviderVerification verification) {
        Optional<ProviderVerification> existing = verificationRepository.findByProviderId(provider.getId());
        
        if (existing.isPresent()) {
            ProviderVerification existingVerification = existing.get();
            updateVerificationFields(existingVerification, verification);
            return verificationRepository.save(existingVerification);
        } else {
            verification.setProvider(provider);
            verification.setVerificationStatus(ProviderVerification.VerificationStatus.PENDING);
            return verificationRepository.save(verification);
        }
    }
    
    public ProviderVerification approveVerification(Long verificationId, Long adminId, String notes) {
        ProviderVerification verification = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        verification.setVerificationStatus(ProviderVerification.VerificationStatus.VERIFIED);
        verification.setVerifiedBy(adminId);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerificationNotes(notes);
        
        return verificationRepository.save(verification);
    }
    
    public ProviderVerification rejectVerification(Long verificationId, Long adminId, String notes) {
        ProviderVerification verification = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        verification.setVerificationStatus(ProviderVerification.VerificationStatus.REJECTED);
        verification.setVerifiedBy(adminId);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerificationNotes(notes);
        
        return verificationRepository.save(verification);
    }
    
    public List<ProviderVerification> getPendingVerifications() {
        return verificationRepository.findByVerificationStatus(ProviderVerification.VerificationStatus.PENDING);
    }
    
    public List<ProviderVerification> getVerificationsByStatus(ProviderVerification.VerificationStatus status) {
        return verificationRepository.findByVerificationStatus(status);
    }
    
    private void updateVerificationFields(ProviderVerification existing, ProviderVerification updated) {
        if (updated.getLicenseNumber() != null) existing.setLicenseNumber(updated.getLicenseNumber());
        if (updated.getLicenseIssueDate() != null) existing.setLicenseIssueDate(updated.getLicenseIssueDate());
        if (updated.getLicenseExpiryDate() != null) existing.setLicenseExpiryDate(updated.getLicenseExpiryDate());
        if (updated.getSpecialization() != null) existing.setSpecialization(updated.getSpecialization());
        if (updated.getQualifications() != null) existing.setQualifications(updated.getQualifications());
        if (updated.getYearsOfExperience() != null) existing.setYearsOfExperience(updated.getYearsOfExperience());
        if (updated.getBio() != null) existing.setBio(updated.getBio());
        if (updated.getDocumentUrls() != null) existing.setDocumentUrls(updated.getDocumentUrls());
    }
}
