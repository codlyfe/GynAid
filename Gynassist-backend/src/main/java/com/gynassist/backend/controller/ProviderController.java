package com.gynassist.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gynassist.backend.dto.ProviderLocationDto;
import com.gynassist.backend.entity.ProviderLocation;
import com.gynassist.backend.service.LocationService;

import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final LocationService locationService;

    /**
     * Updates location for the authenticated provider.
     */
    @PostMapping("/location/update")
    public ResponseEntity<Map<String, String>> updateLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            Authentication authentication) {

        var user = (com.gynassist.backend.entity.User) authentication.getPrincipal();
        locationService.updateProviderLocation(user.getId(), latitude, longitude);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Location updated successfully");
        response.put("providerId", user.getId().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Updates location for a provider by ID (admin or system-level access).
     */
    @PostMapping("/{id}/location")
    public ResponseEntity<?> updateLocationById(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        locationService.updateProviderLocation(id, latitude, longitude);
        return ResponseEntity.ok("Location updated successfully");
    }

    /**
     * Retrieves nearby providers based on location and radius.
     */
    @GetMapping("/location/nearby")
    public ResponseEntity<List<ProviderLocationDto>> getNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {

        List<ProviderLocation> providers = locationService.findNearbyProviders(latitude, longitude, radiusKm);
        List<ProviderLocationDto> dtos = providers.stream()
            .map(ProviderLocationDto::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Returns dashboard data for the authenticated provider.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getProviderDashboard(Authentication authentication) {
        var user = (com.gynassist.backend.entity.User) authentication.getPrincipal();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("provider", user);
        dashboard.put("stats", Map.of(
            "totalConsultations", 0,
            "pendingRequests", 0,
            "monthlyEarnings", 0,
            "averageRating", 0.0
        ));

        return ResponseEntity.ok(dashboard);
    }
}
