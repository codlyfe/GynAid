package com.gynaid.backend.controller;

import com.gynaid.backend.data.UgandaDistricts;
import com.gynaid.backend.dto.ProviderLocationDto;
import com.gynaid.backend.entity.*;
import com.gynaid.backend.repository.*;
import com.gynaid.backend.service.LocationService;
import com.gynaid.backend.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/providers")
@PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private final ProviderRepository providerRepository;
    private final LocationService locationService;
    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Search providers with advanced filtering and pagination
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> searchProviders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String district,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedOnly,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "rating") String sortBy) {
        
        try {
            log.info("Searching providers with query: {}, specialization: {}, district: {}",
                     query, specialization, district);
            
            // Create pageable with sorting
            Sort sort = getSort(sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Default to Uganda-only search if no geographic filter provided
            Boolean ugandaOnly = (district != null && UgandaDistricts.isValidDistrict(district)) ||
                                (query != null && UgandaDistricts.isMajorCity(query));
            
            Page<Provider> providers;
            
            if (ugandaOnly) {
                // Use Uganda-specific search
                providers = providerRepository.advancedSearch(
                    query, specialization, district, verifiedOnly, pageable);
            } else {
                // General search without geographic restrictions
                providers = providerRepository.advancedSearch(
                    query, specialization, null, verifiedOnly, pageable);
            }
            
            // Transform to response DTOs
            List<ProviderSearchResult> results = providers.getContent().stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", results);
            response.put("totalPages", providers.getTotalPages());
            response.put("totalElements", providers.getTotalElements());
            response.put("currentPage", providers.getNumber());
            response.put("size", providers.getSize());
            response.put("hasNext", providers.hasNext());
            response.put("hasPrevious", providers.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching providers", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Search failed: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get providers by specialization
     */
    @GetMapping("/specialization/{specialization}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProvidersBySpecialization(
            @PathVariable String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("user.practiceInfo.rating").descending());
            Page<Provider> providers = providerRepository.findBySpecialization(specialization, pageable);
            
            List<ProviderSearchResult> results = providers.getContent().stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", results);
            response.put("totalPages", providers.getTotalPages());
            response.put("totalElements", providers.getTotalElements());
            response.put("currentPage", providers.getNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting providers by specialization", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get top-rated providers
     */
    @GetMapping("/top-rated")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTopRatedProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Provider> providers = providerRepository.findTopRatedProviders(pageable);
            
            List<ProviderSearchResult> results = providers.getContent().stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", results);
            response.put("totalPages", providers.getTotalPages());
            response.put("totalElements", providers.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting top-rated providers", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get nearby providers using geographic search
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "25.0") Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Point point = locationService.createPoint(latitude, longitude);
            List<Provider> nearbyProviders = providerRepository.findNearbyProviders(point, radiusKm);
            
            // Apply pagination manually for nearby providers
            int start = page * size;
            int end = Math.min(start + size, nearbyProviders.size());
            List<Provider> pagedProviders = nearbyProviders.subList(start, end);
            
            List<ProviderSearchResult> results = pagedProviders.stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", results);
            response.put("totalElements", nearbyProviders.size());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) nearbyProviders.size() / size));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting nearby providers", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get available providers (Uganda default)
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAvailableProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            // Default to Uganda districts for geographic filtering
            Pageable pageable = PageRequest.of(page, size, Sort.by("user.practiceInfo.rating").descending());
            Page<Provider> providers = providerRepository.findProvidersInUganda(
                UgandaDistricts.ALL_DISTRICTS, pageable);
            
            // Filter for only available providers
            List<Provider> availableProviders = providers.getContent().stream()
                .filter(p -> p.getCurrentLocation() != null &&
                           p.getCurrentLocation().getAvailabilityStatus() == ProviderLocation.AvailabilityStatus.AVAILABLE)
                .collect(Collectors.toList());
            
            // Apply pagination to filtered results
            int start = page * size;
            int end = Math.min(start + size, availableProviders.size());
            List<Provider> pagedProviders = availableProviders.subList(start, Math.min(end, availableProviders.size()));
            
            List<ProviderSearchResult> results = pagedProviders.stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", results);
            response.put("totalElements", availableProviders.size());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) availableProviders.size() / size));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting available providers", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get provider statistics (for admin dashboard)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProviderStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProviders", providerRepository.count());
            stats.put("verifiedProviders", providerRepository.countVerifiedProviders());
            stats.put("availableProviders", providerRepository.countAvailableProviders());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting provider statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update location for authenticated provider
     */
    @PostMapping("/location/update")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            locationService.updateProviderLocation(user.getId(), latitude, longitude);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Location updated successfully");
            response.put("providerId", user.getId().toString());
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating provider location", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to update location: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get dashboard data for authenticated provider
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> getProviderDashboard(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            // Find Provider associated with this User
            Provider provider = providerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Provider not found for user: " + user.getId()));
            
            // Calculate provider statistics
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
            
            // Count total consultations for this provider
            int totalConsultations = consultationRepository.findByProviderIdOrderByScheduledDateTimeAsc(user.getId())
                .size();
            
            // Count pending appointment requests
            Pageable pageable = PageRequest.of(0, 1); // Just get count
            int pendingAppointments = (int) appointmentRepository.findByProviderAndStatus(user,
                Appointment.AppointmentStatus.PENDING, pageable).getTotalElements();
            
            // Calculate monthly earnings from completed consultations with payments
            BigDecimal monthlyEarnings = paymentRepository.getTotalRevenueSince(startOfMonth);
            if (monthlyEarnings == null) {
                monthlyEarnings = BigDecimal.ZERO;
            }
            
            // Subtract platform fees (10%)
            BigDecimal platformFee = monthlyEarnings.multiply(new BigDecimal("0.10"));
            BigDecimal providerShare = monthlyEarnings.subtract(platformFee);
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("provider", convertToProviderProfile(provider));
            dashboard.put("statistics", Map.of(
                "totalConsultations", totalConsultations,
                "pendingRequests", pendingAppointments,
                "monthlyEarnings", providerShare.doubleValue(),
                "averageRating", user.getPracticeInfo() != null ?
                    user.getPracticeInfo().getRating() : 0.0
            ));
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting provider dashboard", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper methods
    private Sort getSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "rating" -> Sort.by("user.practiceInfo.rating").descending();
            case "experience" -> Sort.by("user.practiceInfo.yearsOfExperience").descending();
            case "fee" -> Sort.by("user.practiceInfo.consultationFee").ascending();
            case "name" -> Sort.by("user.firstName").ascending();
            default -> Sort.by("user.practiceInfo.rating").descending();
        };
    }

    private ProviderSearchResult convertToSearchResult(Provider provider) {
        return ProviderSearchResult.builder()
            .id(provider.getId())
            .name(getProviderName(provider))
            .email(provider.getEmail())
            .phoneNumber(provider.getPhoneNumber())
            .type(provider.getRole().name())
            .specializations(getProviderSpecializations(provider))
            .yearsExperience(getProviderExperience(provider))
            .rating(getProviderRating(provider))
            .reviewCount(getProviderReviewCount(provider))
            .consultationFee(getProviderFee(provider))
            .location(getProviderLocation(provider))
            .availabilityStatus(getAvailabilityStatus(provider))
            .languages(getProviderLanguages(provider))
            .services(getProviderServices(provider))
            .verified(isProviderVerified(provider))
            .distance(calculateDistanceToUser(provider))
            .build();
    }

    private String getProviderName(Provider provider) {
        return provider.getFirstName() + " " + provider.getLastName();
    }

    private List<String> getProviderSpecializations(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getSpecializations() : Collections.emptyList();
    }

    private int getProviderExperience(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getYearsOfExperience() : 0;
    }

    private double getProviderRating(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getRating() : 0.0;
    }

    private int getProviderReviewCount(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getReviewCount() : 0;
    }

    private double getProviderFee(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getConsultationFee() : 0.0;
    }

    private ProviderSearchResult.Location getProviderLocation(Provider provider) {
        if (provider.getCurrentLocation() != null) {
            return ProviderSearchResult.Location.builder()
                .district(provider.getCurrentLocation().getDistrict())
                .region(UgandaDistricts.getRegionByDistrict(
                    provider.getCurrentLocation().getDistrict()))
                .country("Uganda")
                .build();
        }
        return ProviderSearchResult.Location.builder()
            .country("Uganda")
            .build();
    }

    private String getAvailabilityStatus(Provider provider) {
        return provider.getCurrentLocation() != null ?
            provider.getCurrentLocation().getAvailabilityStatus().name() : "UNKNOWN";
    }

    private List<String> getProviderLanguages(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getLanguages() : Collections.emptyList();
    }

    private List<String> getProviderServices(Provider provider) {
        return provider.getPracticeInfo() != null ?
            provider.getPracticeInfo().getServices() : Collections.emptyList();
    }

    private boolean isProviderVerified(Provider provider) {
        return provider.getProviderVerification() != null &&
               provider.getProviderVerification().isVerified();
    }

    /**
     * Calculate geographical distance between user and provider using Haversine formula.
     * Uses Kampala city center as default user location until user location tracking is implemented.
     */
    private double calculateDistanceToUser(Provider provider) {
        try {
            // Get provider's location
            ProviderLocation providerLocation = provider.getCurrentLocation();
            if (providerLocation == null || providerLocation.getCoordinates() == null) {
                return 0.0; // No location data available
            }
            
            Point providerPoint = providerLocation.getCoordinates();
            double providerLat = LocationUtils.getLatitude(providerPoint);
            double providerLon = LocationUtils.getLongitude(providerPoint);
            
            // Using Kampala city center as default user location (0.3476° N, 32.5825° E)
            // In production, this should be replaced with authenticated user's actual location
            double userLat = 0.3476;
            double userLon = 32.5825;
            
            // Calculate distance using Haversine formula
            return LocationUtils.calculateDistanceKm(userLat, userLon, providerLat, providerLon);
        } catch (Exception e) {
            log.warn("Error calculating distance for provider {}: {}", provider.getId(), e.getMessage());
            return 0.0;
        }
    }

    private Map<String, Object> convertToProviderProfile(Provider provider) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", provider.getId());
        profile.put("name", getProviderName(provider));
        profile.put("email", provider.getEmail());
        profile.put("phoneNumber", provider.getPhoneNumber());
        profile.put("specializations", getProviderSpecializations(provider));
        profile.put("experience", getProviderExperience(provider));
        profile.put("rating", getProviderRating(provider));
        profile.put("consultationFee", getProviderFee(provider));
        profile.put("verified", isProviderVerified(provider));
        profile.put("location", getProviderLocation(provider));
        return profile;
    }

    // Response DTOs
    @lombok.Data
    @lombok.Builder
    public static class ProviderSearchResult {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String type;
        private List<String> specializations;
        private int yearsExperience;
        private double rating;
        private int reviewCount;
        private double consultationFee;
        private Location location;
        private String availabilityStatus;
        private List<String> languages;
        private List<String> services;
        private boolean verified;
        private double distance; // in kilometers
        
        @lombok.Data
        @lombok.Builder
        public static class Location {
            private String district;
            private String region;
            private String country;
        }
    }
}
