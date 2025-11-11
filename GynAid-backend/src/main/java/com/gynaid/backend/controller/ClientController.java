package com.gynaid.backend.controller;

import com.gynaid.backend.dto.ProviderLocationDto;
import com.gynaid.backend.entity.ProviderLocation;
import com.gynaid.backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final LocationService locationService;

    @GetMapping("/providers/nearby")
    public ResponseEntity<List<ProviderLocationDto>> searchNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable) {
        
        // Parse service type if provided
        ProviderLocation.ServiceType serviceType = null;
        if (specialization != null && !specialization.isEmpty()) {
            try {
                serviceType = ProviderLocation.ServiceType.valueOf(specialization.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid service type, ignore and search all
            }
        }
        
        List<ProviderLocation> providers = locationService.findNearbyProviders(
            latitude, longitude, radiusKm, serviceType, onlyAvailable
        );
        
        List<ProviderLocationDto> dtos = providers.stream()
            .map(ProviderLocationDto::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getClientDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("recentSearches", List.of());
        dashboard.put("favoriteProviders", List.of());
        dashboard.put("upcomingAppointments", List.of());
        dashboard.put("healthStats", Map.of(
            "lastCheckup", "2024-01-15",
            "nextAppointment", "2024-02-20",
            "activePrescriptions", 2
        ));
        
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/service-request")
    public ResponseEntity<Map<String, String>> createServiceRequest(
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Service request created successfully");
        response.put("requestId", "REQ_" + System.currentTimeMillis());
        response.put("status", "PENDING");
        
        return ResponseEntity.ok(response);
    }
}
