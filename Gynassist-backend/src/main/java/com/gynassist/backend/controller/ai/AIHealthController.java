package com.gynassist.backend.controller.ai;

import com.gynassist.backend.entity.ai.HealthInsight;
import com.gynassist.backend.entity.ai.SymptomAnalysis;
import com.gynassist.backend.service.ai.AIHealthAssistantService;
import com.gynassist.backend.service.ai.PredictiveAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIHealthController {
    
    private final AIHealthAssistantService aiHealthAssistantService;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    
    @PostMapping("/analyze-symptoms")
    public ResponseEntity<SymptomAnalysis> analyzeSymptoms(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String symptoms = request.get("symptoms");
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        SymptomAnalysis analysis = aiHealthAssistantService.analyzeSymptoms(userId, symptoms);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/insights/{userId}")
    public ResponseEntity<List<HealthInsight>> getPersonalizedInsights(@PathVariable Long userId) {
        List<HealthInsight> insights = aiHealthAssistantService.generatePersonalizedInsights(userId);
        return ResponseEntity.ok(insights);
    }
    
    @GetMapping("/predict/next-period/{userId}")
    public ResponseEntity<Map<String, Object>> predictNextPeriod(@PathVariable Long userId) {
        LocalDate prediction = predictiveAnalyticsService.predictNextPeriod(userId);
        double regularityScore = predictiveAnalyticsService.calculateCycleRegularityScore(userId);
        
        return ResponseEntity.ok(Map.of(
            "nextPeriodDate", prediction != null ? prediction.toString() : null,
            "regularityScore", regularityScore,
            "confidence", regularityScore > 0.7 ? "High" : regularityScore > 0.4 ? "Medium" : "Low"
        ));
    }
    
    @GetMapping("/fertility-insights/{userId}")
    public ResponseEntity<Map<String, String>> getFertilityInsights(@PathVariable Long userId) {
        String insight = predictiveAnalyticsService.generateFertilityInsight(userId);
        return ResponseEntity.ok(Map.of("insight", insight));
    }
    
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAI(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Simple AI chat response (placeholder for advanced AI integration)
        String response = generateAIResponse(query);
        
        return ResponseEntity.ok(Map.of(
            "response", response,
            "disclaimer", "This is AI-generated information. Consult healthcare providers for medical advice."
        ));
    }
    
    private String generateAIResponse(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("period") || lowerQuery.contains("menstruation")) {
            return "Menstrual cycles typically range from 21-35 days. Track your cycles to understand your pattern. " +
                   "Consult a healthcare provider if you notice significant changes or irregularities.";
        } else if (lowerQuery.contains("fertility") || lowerQuery.contains("pregnant")) {
            return "Fertility varies by individual. The fertile window is typically around ovulation, " +
                   "about 14 days before your next period. For personalized advice, consult a fertility specialist.";
        } else if (lowerQuery.contains("pain") || lowerQuery.contains("cramp")) {
            return "Mild menstrual discomfort is normal, but severe pain may indicate underlying conditions. " +
                   "Try heat therapy, gentle exercise, and over-the-counter pain relief. See a provider for severe pain.";
        } else if (lowerQuery.contains("irregular") || lowerQuery.contains("missed")) {
            return "Irregular cycles can be caused by stress, weight changes, hormones, or medical conditions. " +
                   "Track your cycles for 3 months and discuss patterns with your healthcare provider.";
        } else {
            return "I'm here to help with reproductive health questions. For specific medical concerns, " +
                   "please consult with a qualified healthcare provider who can give personalized advice.";
        }
    }
}