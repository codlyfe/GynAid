package com.gynaid.backend.controller;

import com.gynaid.backend.dto.ApiResponse;
import com.gynaid.backend.dto.client.HealthProfileDto;
import com.gynaid.backend.service.ClientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientProfileController {
    
    private final ClientProfileService clientProfileService;
    
    @GetMapping("/health")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<HealthProfileDto>> getHealthProfile(Authentication auth) {
        String email = auth.getName();
        HealthProfileDto profile = clientProfileService.getHealthProfile(email);
        return ResponseEntity.ok(ApiResponse.<HealthProfileDto>builder()
                .success(true)
                .data(profile)
                .message("Health profile retrieved successfully")
                .build());
    }
    
    @PostMapping("/health")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<HealthProfileDto>> createOrUpdateHealthProfile(
            @RequestBody HealthProfileDto profileDto,
            Authentication auth) {
        String email = auth.getName();
        HealthProfileDto profile = clientProfileService.createOrUpdateHealthProfile(email, profileDto);
        return ResponseEntity.ok(ApiResponse.<HealthProfileDto>builder()
                .success(true)
                .data(profile)
                .message("Health profile updated successfully")
                .build());
    }
    
    @GetMapping("/completion")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Integer>> getProfileCompletion(Authentication auth) {
        String email = auth.getName();
        Integer completion = clientProfileService.getProfileCompletionPercentage(email);
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .data(completion)
                .message("Profile completion retrieved successfully")
                .build());
    }
}
