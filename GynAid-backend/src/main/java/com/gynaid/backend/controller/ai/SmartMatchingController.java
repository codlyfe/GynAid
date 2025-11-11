package com.gynaid.backend.controller.ai;

import com.gynaid.backend.entity.ai.ProviderMatch;
import com.gynaid.backend.service.ai.SmartMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/matching")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SmartMatchingController {
    
    private final SmartMatchingService smartMatchingService;
    
    @GetMapping("/providers/{userId}")
    public ResponseEntity<List<ProviderMatch>> getMatchingProviders(@PathVariable Long userId) {
        List<ProviderMatch> matches = smartMatchingService.findMatchingProviders(userId);
        return ResponseEntity.ok(matches);
    }
    
    @GetMapping("/specialists/{userId}")
    public ResponseEntity<List<ProviderMatch>> getSpecialistsByCondition(
            @PathVariable Long userId,
            @RequestParam String condition) {
        
        List<ProviderMatch> specialists = smartMatchingService.findSpecialistsByCondition(userId, condition);
        return ResponseEntity.ok(specialists);
    }
    
    @PostMapping("/treatment-recommendations")
    public ResponseEntity<Map<String, Object>> getTreatmentRecommendations(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String condition = request.get("condition");
        if (condition == null || condition.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<String> recommendations = smartMatchingService.generateTreatmentRecommendations(userId, condition);
        
        return ResponseEntity.ok(Map.of(
            "condition", condition,
            "recommendations", recommendations,
            "disclaimer", "These are AI-generated suggestions. Always consult healthcare providers for medical advice."
        ));
    }
}
