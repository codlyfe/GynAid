package com.gynaid.backend.repository.provider;

import com.gynaid.backend.entity.provider.ProviderVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderVerificationRepository extends JpaRepository<ProviderVerification, Long> {
    Optional<ProviderVerification> findByProviderId(Long providerId);
    List<ProviderVerification> findByVerificationStatus(ProviderVerification.VerificationStatus status);
}


