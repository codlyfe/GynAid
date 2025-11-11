package com.gynaid.backend.service;

import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.repository.HealthcareProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HealthcareProviderService {

    private final HealthcareProviderRepository providerRepository;

    // Core search functionality with geographic priority
    public Page<HealthcareProvider> findProvidersBySpecialization(
            HealthcareProvider.Specialization specialization, 
            int page, 
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return providerRepository.findBySpecializationWithPriority(specialization, pageable);
    }

    // Multi-specialization search
    public Page<HealthcareProvider> findProvidersBySpecializations(
            List<HealthcareProvider.Specialization> specializations,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return providerRepository.findBySpecializationsIn(specializations, pageable);
    }

    // Location-based search
    public List<HealthcareProvider> findNearbyProviders(
            double latitude, 
            double longitude, 
            double radiusKm) {
        return providerRepository.findNearbyProviders(latitude, longitude, radiusKm);
    }

    // District-based search (Uganda focus)
    public List<HealthcareProvider> findProvidersByDistrict(String district) {
        return providerRepository.findByDistrict(district);
    }

    // Available providers
    public List<HealthcareProvider> findAvailableProviders() {
        return providerRepository.findByAvailabilityStatusAndVerificationStatus(
            HealthcareProvider.AvailabilityStatus.AVAILABLE,
            HealthcareProvider.VerificationStatus.VERIFIED
        );
    }

    // Search providers by name or services
    public Page<HealthcareProvider> searchProviders(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return providerRepository.searchProviders(searchTerm, pageable);
    }

    // Provider registration (for new providers)
    @Transactional
    public HealthcareProvider registerProvider(HealthcareProvider provider) {
        provider.setVerificationStatus(HealthcareProvider.VerificationStatus.PENDING);
        provider.setAvailabilityStatus(HealthcareProvider.AvailabilityStatus.OFFLINE);
        return providerRepository.save(provider);
    }

    // Admin: Verify provider
    @Transactional
    public HealthcareProvider verifyProvider(Long providerId) {
        Optional<HealthcareProvider> providerOpt = providerRepository.findById(providerId);
        if (providerOpt.isPresent()) {
            HealthcareProvider provider = providerOpt.get();
            provider.setVerificationStatus(HealthcareProvider.VerificationStatus.VERIFIED);
            provider.setUpdatedAt(LocalDateTime.now());
            return providerRepository.save(provider);
        }
        throw new RuntimeException("Provider not found");
    }

    // Admin: Get pending verifications
    public List<HealthcareProvider> getPendingVerifications() {
        return providerRepository.findByVerificationStatus(HealthcareProvider.VerificationStatus.PENDING);
    }

    // Update provider availability
    @Transactional
    public HealthcareProvider updateAvailability(Long providerId, HealthcareProvider.AvailabilityStatus status) {
        Optional<HealthcareProvider> providerOpt = providerRepository.findById(providerId);
        if (providerOpt.isPresent()) {
            HealthcareProvider provider = providerOpt.get();
            provider.setAvailabilityStatus(status);
            provider.setLastActiveAt(LocalDateTime.now());
            return providerRepository.save(provider);
        }
        throw new RuntimeException("Provider not found");
    }

    // Get provider statistics
    public ProviderStatistics getProviderStatistics() {
        long ugandaCount = providerRepository.countByScope(HealthcareProvider.GeographicScope.UGANDA);
        long eastAfricaCount = providerRepository.countByScope(HealthcareProvider.GeographicScope.EAST_AFRICA);
        long africaCount = providerRepository.countByScope(HealthcareProvider.GeographicScope.AFRICA);
        long globalCount = providerRepository.countByScope(HealthcareProvider.GeographicScope.GLOBAL);
        
        return ProviderStatistics.builder()
            .ugandaProviders(ugandaCount)
            .eastAfricaProviders(eastAfricaCount)
            .africaProviders(africaCount)
            .globalProviders(globalCount)
            .totalProviders(ugandaCount + eastAfricaCount + africaCount + globalCount)
            .build();
    }

    // Smart matching based on user profile and condition
    public List<HealthcareProvider> getSmartRecommendations(
            String userDistrict,
            List<HealthcareProvider.Specialization> requiredSpecializations,
            double maxDistance) {
        
        // Priority 1: Same district providers
        List<HealthcareProvider> districtProviders = findProvidersByDistrict(userDistrict);
        if (!districtProviders.isEmpty()) {
            return districtProviders.stream()
                .filter(p -> p.getSpecializations().stream()
                    .anyMatch(requiredSpecializations::contains))
                .limit(10)
                .toList();
        }
        
        // Priority 2: Uganda providers with specialization
        return findProvidersBySpecializations(requiredSpecializations, 0, 10).getContent();
    }

    @lombok.Data
    @lombok.Builder
    public static class ProviderStatistics {
        private long ugandaProviders;
        private long eastAfricaProviders;
        private long africaProviders;
        private long globalProviders;
        private long totalProviders;
    }
}
