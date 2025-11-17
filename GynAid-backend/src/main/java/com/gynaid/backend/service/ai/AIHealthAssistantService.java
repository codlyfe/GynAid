package com.gynaid.backend.service.ai;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.ai.HealthInsight;
import com.gynaid.backend.entity.ai.SymptomAnalysis;
import com.gynaid.backend.entity.client.GynecologicalProfile;
import com.gynaid.backend.repository.UserRepository;
import com.gynaid.backend.repository.client.ClientHealthProfileRepository;
import com.gynaid.backend.service.GynecologicalProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIHealthAssistantService {
    
    private final UserRepository userRepository;
    private final ClientHealthProfileRepository healthProfileRepository;
    private final GynecologicalProfileService gynecologicalProfileService;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    
    public SymptomAnalysis analyzeSymptoms(Long userId, String symptoms) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Enhanced symptom analysis preserving existing logic
        SymptomAnalysis analysis = SymptomAnalysis.builder()
            .user(user)
            .symptoms(symptoms)
            .build();
        
        // AI-powered analysis (placeholder for actual AI integration)
        String aiAnalysis = generateSymptomAnalysis(symptoms);
        SymptomAnalysis.RiskLevel riskLevel = assessRiskLevel(symptoms);
        List<String> recommendations = generateRecommendations(symptoms, riskLevel);
        
        analysis.setAiAnalysis(aiAnalysis);
        analysis.setRiskLevel(riskLevel);
        analysis.setRecommendations(recommendations);
        analysis.setRequiresProviderAttention(riskLevel == SymptomAnalysis.RiskLevel.HIGH || 
                                            riskLevel == SymptomAnalysis.RiskLevel.EMERGENCY);
        
        return analysis;
    }
    
    public List<HealthInsight> generatePersonalizedInsights(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<HealthInsight> insights = new ArrayList<>();
        
        // Generate cycle prediction insights (enhancing existing predictions)
        try {
            LocalDate nextPeriod = predictiveAnalyticsService.predictNextPeriod(userId);
            if (nextPeriod != null) {
                insights.add(HealthInsight.builder()
                    .user(user)
                    .type(HealthInsight.InsightType.CYCLE_PREDICTION)
                    .content("Your next period is predicted for " + nextPeriod + 
                           ". Track symptoms to improve accuracy.")
                    .confidence(0.85)
                    .priority(HealthInsight.Priority.MEDIUM)
                    .build());
            }
        } catch (Exception e) {
            log.warn("Could not generate cycle prediction for user {}: {}", userId, e.getMessage());
        }
        
        // Generate fertility window insights
        try {
            GynecologicalProfile profile = getGynecologicalProfileByUserId(userId);
            if (profile != null && profile.getFertilityGoal() == GynecologicalProfile.FertilityGoal.TRYING_TO_CONCEIVE) {
                insights.add(generateFertilityInsight(user, profile));
            }
        } catch (Exception e) {
            log.warn("Could not generate fertility insights for user {}: {}", userId, e.getMessage());
        }
        
        return insights;
    }
    
    private GynecologicalProfile getGynecologicalProfileByUserId(Long userId) {
        try {
            // For AI purposes, we need the entity, so we'll fetch it directly
            return healthProfileRepository.findByUserId(userId)
                .map(hp -> (GynecologicalProfile) hp.getGynecologicalProfile())
                .orElse(null);
        } catch (Exception e) {
            log.warn("Could not get gynecological profile for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    private String generateSymptomAnalysis(String symptoms) {
        // Placeholder for AI integration - preserves existing medical logic
        String lowerSymptoms = symptoms.toLowerCase();
        
        if (lowerSymptoms.contains("severe pain") || lowerSymptoms.contains("heavy bleeding")) {
            return "Your symptoms may indicate a condition requiring medical attention. Please consult a healthcare provider.";
        } else if (lowerSymptoms.contains("irregular") || lowerSymptoms.contains("missed period")) {
            return "Irregular cycles can have various causes. Consider tracking for a few months and discuss with a provider if patterns persist.";
        } else {
            return "Your symptoms appear to be within normal ranges. Continue monitoring and maintain healthy lifestyle habits.";
        }
    }
    
    private SymptomAnalysis.RiskLevel assessRiskLevel(String symptoms) {
        String lowerSymptoms = symptoms.toLowerCase();
        
        if (lowerSymptoms.contains("severe") || lowerSymptoms.contains("emergency") || 
            lowerSymptoms.contains("unbearable")) {
            return SymptomAnalysis.RiskLevel.EMERGENCY;
        } else if (lowerSymptoms.contains("heavy") || lowerSymptoms.contains("persistent") ||
                  lowerSymptoms.contains("unusual")) {
            return SymptomAnalysis.RiskLevel.HIGH;
        } else if (lowerSymptoms.contains("mild") || lowerSymptoms.contains("occasional")) {
            return SymptomAnalysis.RiskLevel.LOW;
        } else {
            return SymptomAnalysis.RiskLevel.MODERATE;
        }
    }
    
    private List<String> generateRecommendations(String symptoms, SymptomAnalysis.RiskLevel riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        switch (riskLevel) {
            case EMERGENCY:
                recommendations.add("Seek immediate medical attention");
                recommendations.add("Contact emergency services if symptoms worsen");
                break;
            case HIGH:
                recommendations.add("Schedule appointment with healthcare provider within 24-48 hours");
                recommendations.add("Monitor symptoms closely");
                break;
            case MODERATE:
                recommendations.add("Consider scheduling routine check-up");
                recommendations.add("Continue tracking symptoms");
                break;
            case LOW:
                recommendations.add("Maintain healthy lifestyle habits");
                recommendations.add("Continue regular cycle tracking");
                break;
        }
        
        return recommendations;
    }
    
    private HealthInsight generateFertilityInsight(User user, GynecologicalProfile profile) {
        String content = "Based on your cycle data, your fertile window is typically around day " +
                        (profile.getAverageCycleLength() != null ? (profile.getAverageCycleLength() - 14) : 14) +
                        " of your cycle. Track ovulation signs for better timing.";
        
        return HealthInsight.builder()
            .user(user)
            .type(HealthInsight.InsightType.FERTILITY_WINDOW)
            .content(content)
            .confidence(0.75)
            .priority(HealthInsight.Priority.HIGH)
            .build();
    }
}
