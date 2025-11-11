package com.gynaid.backend.controller;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.provider.ProviderPracticeInfo;
import com.gynaid.backend.service.ProviderPracticeInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/provider/practice-info")
@RequiredArgsConstructor
public class ProviderPracticeInfoController {
    
    private final ProviderPracticeInfoService practiceInfoService;
    
    @GetMapping
    public ResponseEntity<ProviderPracticeInfo> getPracticeInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ProviderPracticeInfo> practiceInfo = practiceInfoService.getPracticeInfo(user.getId());
        
        return practiceInfo.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ProviderPracticeInfo> createOrUpdatePracticeInfo(
            @RequestBody ProviderPracticeInfo practiceInfo,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Only allow providers to create/update their own practice info
        if (!isProvider(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderPracticeInfo savedInfo = practiceInfoService.createOrUpdatePracticeInfo(user, practiceInfo);
        return ResponseEntity.ok(savedInfo);
    }
    
    @PutMapping("/fees")
    public ResponseEntity<ProviderPracticeInfo> updateConsultationFees(
            @RequestParam(required = false) Double virtualFee,
            @RequestParam(required = false) Double inPersonFee,
            @RequestParam(required = false) Double homeVisitFee,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!isProvider(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderPracticeInfo updatedInfo = practiceInfoService.updateConsultationFees(
            user.getId(), virtualFee, inPersonFee, homeVisitFee);
        return ResponseEntity.ok(updatedInfo);
    }
    
    @PutMapping("/payment-methods")
    public ResponseEntity<ProviderPracticeInfo> updatePaymentMethods(
            @RequestParam(required = false) String paymentMethods,
            @RequestParam(required = false) String mobileMoneyNumber,
            @RequestParam(required = false) ProviderPracticeInfo.MobileMoneyProvider mobileMoneyProvider,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!isProvider(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderPracticeInfo updatedInfo = practiceInfoService.updatePaymentMethods(
            user.getId(), paymentMethods, mobileMoneyNumber, mobileMoneyProvider);
        return ResponseEntity.ok(updatedInfo);
    }
    
    @PutMapping("/stripe")
    public ResponseEntity<ProviderPracticeInfo> updateStripeAccount(
            @RequestParam(required = false) String stripeAccountId,
            @RequestParam(required = false) Boolean verified,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!isProvider(user)) {
            return ResponseEntity.status(403).build();
        }
        
        ProviderPracticeInfo updatedInfo = practiceInfoService.updateStripeAccount(
            user.getId(), stripeAccountId, verified);
        return ResponseEntity.ok(updatedInfo);
    }
    
    private boolean isProvider(User user) {
        return user.getRole() == User.UserRole.PROVIDER_INDIVIDUAL || 
               user.getRole() == User.UserRole.PROVIDER_INSTITUTION;
    }
}
