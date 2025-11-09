package com.gynassist.backend.controller;

import com.gynassist.backend.entity.HealthcareProvider;
import com.gynassist.backend.service.HealthcareProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class HealthcareProviderController {

    private final HealthcareProviderService providerService;

    // Public endpoints for finding providers
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<Page<HealthcareProvider>> getProvidersBySpecialization(
            @PathVariable HealthcareProvider.Specialization specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<HealthcareProvider> providers = providerService.findProvidersBySpecialization(specialization, page, size);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/district/{district}")
    public ResponseEntity<List<HealthcareProvider>> getProvidersByDistrict(@PathVariable String district) {
        List<HealthcareProvider> providers = providerService.findProvidersByDistrict(district);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<HealthcareProvider>> getNearbyProviders(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "50") double radiusKm) {
        
        List<HealthcareProvider> providers = providerService.findNearbyProviders(latitude, longitude, radiusKm);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/available")
    public ResponseEntity<List<HealthcareProvider>> getAvailableProviders() {
        List<HealthcareProvider> providers = providerService.findAvailableProviders();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HealthcareProvider>> searchProviders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<HealthcareProvider> providers = providerService.searchProviders(query, page, size);
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/recommendations")
    public ResponseEntity<List<HealthcareProvider>> getRecommendations(
            @RequestBody RecommendationRequest request) {
        
        List<HealthcareProvider> providers = providerService.getSmartRecommendations(
            request.getUserDistrict(),
            request.getSpecializations(),
            request.getMaxDistance()
        );
        return ResponseEntity.ok(providers);
    }

    // Provider registration endpoint
    @PostMapping("/register")
    public ResponseEntity<HealthcareProvider> registerProvider(@RequestBody HealthcareProvider provider) {
        HealthcareProvider registered = providerService.registerProvider(provider);
        return ResponseEntity.ok(registered);
    }

    // Provider self-service endpoints
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<HealthcareProvider> updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityUpdateRequest request) {
        
        HealthcareProvider updated = providerService.updateAvailability(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // Admin endpoints
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HealthcareProvider>> getPendingVerifications() {
        List<HealthcareProvider> pending = providerService.getPendingVerifications();
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/admin/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthcareProvider> verifyProvider(@PathVariable Long id) {
        HealthcareProvider verified = providerService.verifyProvider(id);
        return ResponseEntity.ok(verified);
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthcareProviderService.ProviderStatistics> getStatistics() {
        HealthcareProviderService.ProviderStatistics stats = providerService.getProviderStatistics();
        return ResponseEntity.ok(stats);
    }

    // DTOs
    @lombok.Data
    public static class RecommendationRequest {
        private String userDistrict;
        private List<HealthcareProvider.Specialization> specializations;
        private double maxDistance = 100.0; // Default 100km
    }

    @lombok.Data
    public static class AvailabilityUpdateRequest {
        private HealthcareProvider.AvailabilityStatus status;
    }
}