package com.gynassist.backend.repository;

import com.gynassist.backend.entity.HealthcareProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthcareProviderRepository extends JpaRepository<HealthcareProvider, Long> {

    // Find by specialization with geographic priority
    @Query("SELECT p FROM HealthcareProvider p WHERE :specialization MEMBER OF p.specializations " +
           "AND p.verificationStatus = 'VERIFIED' " +
           "ORDER BY " +
           "CASE p.scope " +
           "WHEN 'UGANDA' THEN 1 " +
           "WHEN 'EAST_AFRICA' THEN 2 " +
           "WHEN 'AFRICA' THEN 3 " +
           "WHEN 'GLOBAL' THEN 4 " +
           "END, p.rating DESC, p.reviewCount DESC")
    Page<HealthcareProvider> findBySpecializationWithPriority(
        @Param("specialization") HealthcareProvider.Specialization specialization, 
        Pageable pageable);

    // Find by district (Uganda priority)
    @Query("SELECT p FROM HealthcareProvider p WHERE p.district = :district " +
           "AND p.verificationStatus = 'VERIFIED' " +
           "ORDER BY p.rating DESC, p.reviewCount DESC")
    List<HealthcareProvider> findByDistrict(@Param("district") String district);

    // Find by multiple specializations - FIXED VERSION
    @Query("SELECT DISTINCT hp FROM HealthcareProvider hp JOIN hp.specializations s WHERE s IN :specializations")
    Page<HealthcareProvider> findBySpecializationsIn(@Param("specializations") List<HealthcareProvider.Specialization> specializations, Pageable pageable);

    // Geographic search with radius (for location-based search)
    @Query(value = "SELECT * FROM healthcare_providers p WHERE " +
           "ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusKm * 1000) " +
           "AND p.verification_status = 'VERIFIED' " +
           "ORDER BY ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))", 
           nativeQuery = true)
    List<HealthcareProvider> findNearbyProviders(
        @Param("latitude") double latitude, 
        @Param("longitude") double longitude, 
        @Param("radiusKm") double radiusKm);

    // Find by availability status
    List<HealthcareProvider> findByAvailabilityStatusAndVerificationStatus(
        HealthcareProvider.AvailabilityStatus availabilityStatus,
        HealthcareProvider.VerificationStatus verificationStatus);

    // Search by name or services
    @Query("SELECT p FROM HealthcareProvider p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR EXISTS (SELECT s FROM p.services s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) " +
           "AND p.verificationStatus = 'VERIFIED'")
    Page<HealthcareProvider> searchProviders(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Admin queries for provider management
    List<HealthcareProvider> findByVerificationStatus(HealthcareProvider.VerificationStatus status);
    
    Page<HealthcareProvider> findByVerificationStatus(HealthcareProvider.VerificationStatus status, Pageable pageable);
    
    Integer countByVerificationStatus(HealthcareProvider.VerificationStatus status);
    
    @Query("SELECT COUNT(p) FROM HealthcareProvider p WHERE p.scope = :scope")
    Long countByScope(@Param("scope") HealthcareProvider.GeographicScope scope);
    
    // Enhanced search with subscription priority
    @Query("SELECT p FROM HealthcareProvider p LEFT JOIN ProviderSubscription s ON p.id = s.provider.id " +
           "WHERE :specialization MEMBER OF p.specializations " +
           "AND p.verificationStatus = 'VERIFIED' " +
           "ORDER BY " +
           "CASE WHEN s.status = 'ACTIVE' THEN s.priorityRanking ELSE 0 END DESC, " +
           "CASE p.scope " +
           "WHEN 'UGANDA' THEN 1 " +
           "WHEN 'EAST_AFRICA' THEN 2 " +
           "WHEN 'AFRICA' THEN 3 " +
           "WHEN 'GLOBAL' THEN 4 " +
           "END, p.rating DESC")
    Page<HealthcareProvider> findBySpecializationWithSubscriptionPriority(
        @Param("specialization") HealthcareProvider.Specialization specialization, 
        Pageable pageable);
}