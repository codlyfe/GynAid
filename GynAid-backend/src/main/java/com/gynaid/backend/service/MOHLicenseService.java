package com.gynaid.backend.service;

import com.gynaid.backend.entity.MOHLicense;
import com.gynaid.backend.repository.MOHLicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MOHLicenseService {
    
    private final MOHLicenseRepository mohLicenseRepository;
    private final RestTemplate restTemplate;
    
    @Value("${app.moh.api-url}")
    private String mohValidationApi;
    
    @Value("${app.moh.api-key}")
    private String mohApiKey;
    
    @Cacheable(value = "licenseValidations", unless = "#result != T(com.GynaId.entity.MOHLicense$VerificationStatus).VERIFIED")
    public MOHLicense.VerificationStatus validateLicense(String licenseNumber) {
        // Check cache first
        MOHLicense existingLicense = mohLicenseRepository.findByLicenseNumber(licenseNumber);
        if (existingLicense != null && existingLicense.getVerificationStatus() == MOHLicense.VerificationStatus.VERIFIED) {
            if (existingLicense.getExpiryDate().isAfter(LocalDate.now())) {
                return MOHLicense.VerificationStatus.VERIFIED;
            } else {
                return MOHLicense.VerificationStatus.EXPIRED;
            }
        }
        
        // Call MOH API for validation
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + mohApiKey);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("license_number", licenseNumber);
            requestBody.put("validation_type", "FULL");
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                mohValidationApi, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                boolean isValid = (Boolean) responseBody.get("is_valid");
                String status = (String) responseBody.get("license_status");
                
                if (isValid && "ACTIVE".equals(status)) {
                    return MOHLicense.VerificationStatus.VERIFIED;
                } else if ("EXPIRED".equals(status)) {
                    return MOHLicense.VerificationStatus.EXPIRED;
                } else {
                    return MOHLicense.VerificationStatus.SUSPENDED;
                }
            }
        } catch (Exception e) {
            // Log error and fall back to manual verification
            System.err.println("MOH API call failed: " + e.getMessage());
        }
        
        return MOHLicense.VerificationStatus.PENDING;
    }
    
    public MOHLicense registerLicense(MOHLicense license) {
        MOHLicense.VerificationStatus status = validateLicense(license.getLicenseNumber());
        license.setVerificationStatus(status);
        license.setLastVerified(LocalDate.now());
        
        return mohLicenseRepository.save(license);
    }
}
