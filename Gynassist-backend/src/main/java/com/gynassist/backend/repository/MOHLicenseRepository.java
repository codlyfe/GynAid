package com.gynassist.backend.repository;

import com.gynassist.backend.entity.MOHLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MOHLicenseRepository extends JpaRepository<MOHLicense, Long> {
    MOHLicense findByLicenseNumber(String licenseNumber);
    List<MOHLicense> findByUserId(Long userId);
    List<MOHLicense> findByVerificationStatus(MOHLicense.VerificationStatus status);
    Optional<MOHLicense> findByUserIdAndLicenseNumber(Long userId, String licenseNumber);
}