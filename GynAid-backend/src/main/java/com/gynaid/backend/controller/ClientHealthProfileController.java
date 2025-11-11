package com.gynaid.backend.controller;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.client.*;
import com.gynaid.backend.service.ClientHealthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
public class ClientHealthProfileController {
    
    private final ClientHealthProfileService healthProfileService;
    
    @GetMapping
    public ResponseEntity<ClientHealthProfile> getHealthProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ClientHealthProfile> profile = healthProfileService.getHealthProfile(user.getId());
        
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ClientHealthProfile> createOrUpdateHealthProfile(
            @RequestBody ClientHealthProfile profile,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ClientHealthProfile savedProfile = healthProfileService.createOrUpdateHealthProfile(user, profile);
        return ResponseEntity.ok(savedProfile);
    }
    
    @PostMapping("/vitals")
    public ResponseEntity<MedicalVitals> updateMedicalVitals(
            @RequestBody MedicalVitals vitals,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        MedicalVitals savedVitals = healthProfileService.updateMedicalVitals(user.getId(), vitals);
        return ResponseEntity.ok(savedVitals);
    }
    
    @PostMapping("/medical-history")
    public ResponseEntity<MedicalHistory> updateMedicalHistory(
            @RequestBody MedicalHistory history,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        MedicalHistory savedHistory = healthProfileService.updateMedicalHistory(user.getId(), history);
        return ResponseEntity.ok(savedHistory);
    }
    
    @GetMapping("/vitals")
    public ResponseEntity<MedicalVitals> getMedicalVitals(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ClientHealthProfile> profile = healthProfileService.getHealthProfile(user.getId());
        
        return profile.map(p -> p.getMedicalVitals())
                     .map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/medical-history")
    public ResponseEntity<MedicalHistory> getMedicalHistory(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ClientHealthProfile> profile = healthProfileService.getHealthProfile(user.getId());
        
        return profile.map(p -> p.getMedicalHistory())
                     .map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
}
