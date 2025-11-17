package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Provider;
import com.gynaid.backend.entity.ProviderLocation;
import com.gynaid.backend.entity.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    // Find providers by role
    List<Provider> findByUserRole(User.UserRole role);

    // Find providers by role with pagination
    Page<Provider> findByUserRole(User.UserRole role, Pageable pageable);

    // Search providers by name or specializations
    @Query("SELECT p FROM Provider p WHERE " +
           "LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.specialty) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Provider> searchProviders(@Param("query") String query, Pageable pageable);

    // Find providers by specialization
    @Query("SELECT p FROM Provider p WHERE " +
           "LOWER(p.specialty) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    Page<Provider> findBySpecialization(@Param("specialization") String specialization, Pageable pageable);

    // Find providers by location (district/region)
    @Query("SELECT p FROM Provider p WHERE " +
           "p.user.practiceInfo.district = :district OR p.user.practiceInfo.city = :region")
    Page<Provider> findByLocation(@Param("district") String district, @Param("region") String region, Pageable pageable);

    // Find nearby providers using simplified query (no PostGIS functions for H2 compatibility)
    @Query("SELECT p FROM Provider p WHERE " +
           "p.user.practiceInfo.practiceLocation IS NOT NULL")
    List<Provider> findNearbyProviders(@Param("point") Point point, @Param("radius") Double radius);

    // Find verified providers only
    @Query("SELECT p FROM Provider p WHERE p.user.providerVerification.verificationStatus = :status")
    List<Provider> findByProviderVerificationStatus(
        @Param("status") com.gynaid.backend.entity.provider.ProviderVerification.VerificationStatus status);

    // Advanced search with basic filters (removed problematic rating/specialization queries)
    @Query("SELECT p FROM Provider p WHERE " +
           "(:query IS NULL OR " +
           "LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.specialty) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:district IS NULL OR p.user.practiceInfo.district = :district) AND " +
           "(:specialization IS NULL OR LOWER(p.specialty) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
           "(:verifiedOnly IS NULL OR :verifiedOnly = false OR p.user.providerVerification.verificationStatus = 'VERIFIED')")
    Page<Provider> advancedSearch(
        @Param("query") String query,
        @Param("specialization") String specialization,
        @Param("district") String district,
        @Param("verifiedOnly") Boolean verifiedOnly,
        Pageable pageable);

    // Uganda-specific queries (simplified)
    @Query("SELECT p FROM Provider p WHERE " +
           "p.user.practiceInfo.district IN :ugandaDistricts")
    Page<Provider> findProvidersInUganda(@Param("ugandaDistricts") List<String> ugandaDistricts, Pageable pageable);

    // Get provider statistics (simplified)
    @Query("SELECT COUNT(p) FROM Provider p WHERE p.user.providerVerification.verificationStatus = 'VERIFIED'")
    long countVerifiedProviders();

    @Query("SELECT COUNT(p) FROM Provider p WHERE p.isActive = true")
    long countAvailableProviders();

    // Top-rated providers (simplified - order by ID for now)
    @Query("SELECT p FROM Provider p WHERE p.user.providerVerification.verificationStatus = 'VERIFIED' " +
           "ORDER BY p.id DESC")
    Page<Provider> findTopRatedProviders(Pageable pageable);

    // Find provider by user
    Optional<Provider> findByUser(User user);
}

