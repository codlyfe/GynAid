package com.gynaid.backend.repository.provider;

import com.gynaid.backend.entity.provider.ProviderPracticeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderPracticeInfoRepository extends JpaRepository<ProviderPracticeInfo, Long> {
    Optional<ProviderPracticeInfo> findByProviderId(Long providerId);
}


