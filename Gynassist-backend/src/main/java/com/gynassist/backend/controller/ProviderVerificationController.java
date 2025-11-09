package com.gynassist.backend.controller;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.provider.ProviderVerification;
import com.gynassist.backend.service.ProviderVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/provider/verification")
@RequiredArgsConstructor
public class ProviderVerificationController {
    
    private final ProviderVerificationService verificationService;
    
    @GetMapping
    public ResponseEntity<ProviderVerification> getVerification(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ProviderVerification> verification = verificationService.getVerification(user.getId());
        
        return verification.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ProviderVerification> createOrUpdateVerification(
            @RequestBody ProviderVerification verification,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow providers to create/update their own verification
        if (!isProvider(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderVerification savedVerification = verificationService.createOrUpdateVerification(user, verification);
        return ResponseEntity.ok(savedVerification);
    }
    
    @PostMapping("/{verificationId}/approve")
    public ResponseEntity<ProviderVerification> approveVerification(
            @PathVariable Long verificationId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow admins to approve verifications
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderVerification verification = verificationService.approveVerification(verificationId, user.getId(), notes);
        return ResponseEntity.ok(verification);
    }
    
    @PostMapping("/{verificationId}/reject")
    public ResponseEntity<ProviderVerification> rejectVerification(
            @PathVariable Long verificationId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow admins to reject verifications
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderVerification verification = verificationService.rejectVerification(verificationId, user.getId(), notes);
        return ResponseEntity.ok(verification);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<ProviderVerification>> getPendingVerifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow admins to view pending verifications
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).build();
        }
        
        List<ProviderVerification> pendingVerifications = verificationService.getPendingVerifications();
        return ResponseEntity.ok(pendingVerifications);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProviderVerification>> getVerificationsByStatus(
            @PathVariable ProviderVerification.VerificationStatus status,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow admins to view verifications by status
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).build();
        }
        
        List<ProviderVerification> verifications = verificationService.getVerificationsByStatus(status);
        return ResponseEntity.ok(verifications);
    }
    
    private boolean isProvider(User user) {
        return user.getRole() == User.UserRole.PROVIDER_INDIVIDUAL || 
               user.getRole() == User.UserRole.PROVIDER_INSTITUTION;
    }
    
    private boolean isAdmin(User user) {
        return user.getRole() == User.UserRole.ADMIN;
    }
}