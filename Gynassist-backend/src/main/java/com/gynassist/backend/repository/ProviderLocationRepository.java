package com.gynassist.backend.repository;

import com.gynassist.backend.entity.ProviderLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderLocationRepository extends JpaRepository<ProviderLocation, Long> {
    
    /**
     * Finds a provider location by user/provider ID.
     * Uses the provider relationship to find by user ID.
     */
    @Query("SELECT pl FROM ProviderLocation pl WHERE pl.provider.id = :providerId")
    Optional<ProviderLocation> findByProviderId(@Param("providerId") Long providerId);
    
    /**
     * Finds all provider locations with ONLINE or BUSY availability status.
     */
    List<ProviderLocation> findByAvailabilityStatusIn(List<ProviderLocation.AvailabilityStatus> statuses);
    
    /**
     * Finds provider locations by service type.
     */
    List<ProviderLocation> findByServiceType(ProviderLocation.ServiceType serviceType);
}

