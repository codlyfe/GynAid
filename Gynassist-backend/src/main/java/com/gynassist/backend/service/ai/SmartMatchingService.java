package com.gynassist.backend.service.ai;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.ai.ProviderMatch;
import com.gynassist.backend.entity.client.GynecologicalProfile;
import com.gynassist.backend.entity.Provider;
import com.gynassist.backend.repository.UserRepository;
import com.gynassist.backend.repository.client.ClientHealthProfileRepository;
import com.gynassist.backend.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartMatchingService {
    
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ClientHealthProfileRepository healthProfileRepository;
    
    public List<ProviderMatch> findMatchingProviders(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        List<Provider> availableProviders = providerRepository.findAll();
        
        return availableProviders.stream()
            .map(provider -> calculateProviderMatch(user, provider, profile))
            .filter(match -> match.getMatchScore() > 0.3) // Minimum threshold
            .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    public List<ProviderMatch> findSpecialistsByCondition(Long userId, String condition) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Provider> specialists = findSpecialistsForCondition(condition);
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        
        return specialists.stream()
            .map(provider -> {
                ProviderMatch match = calculateProviderMatch(user, provider, profile);
                match.setMatchType(ProviderMatch.MatchType.CONDITION_SPECIALIST);
                match.setMatchReason("Specialist for " + condition);
                return match;
            })
            .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore()))
            .collect(Collectors.toList());
    }
    
    public List<String> generateTreatmentRecommendations(Long userId, String condition) {
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        List<String> recommendations = new ArrayList<>();
        
        // Generate evidence-based recommendations preserving existing medical logic
        switch (condition.toLowerCase()) {
            case "irregular_cycles":
                recommendations.add("Track cycles for 3 months to identify patterns");
                recommendations.add("Maintain healthy lifestyle with regular exercise");
                recommendations.add("Consider stress management techniques");
                if (profile != null && profile.getStressLevel() != null && profile.getStressLevel() > 7) {
                    recommendations.add("High stress levels detected - prioritize stress reduction");
                }
                break;
                
            case "fertility_concerns":
                recommendations.add("Track ovulation signs and fertile window");
                recommendations.add("Maintain healthy weight and nutrition");
                recommendations.add("Consider preconception vitamins");
                if (profile != null && profile.getTryingToConcieveMonths() != null && 
                    profile.getTryingToConcieveMonths() > 12) {
                    recommendations.add("Consider fertility specialist consultation after 12+ months");
                }
                break;
                
            case "menstrual_pain":
                recommendations.add("Use heat therapy during painful periods");
                recommendations.add("Try gentle exercise and stretching");
                recommendations.add("Consider over-the-counter pain relief");
                recommendations.add("Track pain levels to identify severe patterns");
                break;
                
            default:
                recommendations.add("Maintain regular gynecological check-ups");
                recommendations.add("Track symptoms and patterns");
                recommendations.add("Consult healthcare provider for personalized advice");
        }
        
        return recommendations;
    }
    
    private ProviderMatch calculateProviderMatch(User user, Provider provider, GynecologicalProfile profile) {
        double score = 0.0;
        StringBuilder reason = new StringBuilder();
        
        // Base score for active providers
        if (provider.getIsActive()) {
            score += 0.3;
        }
        
        // Specialization matching (preserves existing provider logic)
        if (provider.getSpecialty() != null) {
            String specialty = provider.getSpecialty().toLowerCase();
            if (specialty.contains("gynecolog") || specialty.contains("reproductive")) {
                score += 0.4;
                reason.append("Gynecology specialist. ");
            }
            if (profile != null && profile.getFertilityGoal() == GynecologicalProfile.FertilityGoal.TRYING_TO_CONCEIVE
                && specialty.contains("fertility")) {
                score += 0.3;
                reason.append("Fertility specialist match. ");
            }
        }
        
        // Active provider bonus
        if (provider.getIsActive() != null && provider.getIsActive()) {
            score += 0.1;
            reason.append("Active provider. ");
        }
        
        // Location proximity (simplified - would use actual coordinates in production)
        Double distance = calculateDistance(user, provider);
        if (distance != null && distance <= 10) {
            score += 0.2;
            reason.append("Nearby location. ");
        }
        
        return ProviderMatch.builder()
            .user(user)
            .provider(provider)
            .matchScore(Math.min(1.0, score)) // Cap at 1.0
            .matchReason(reason.toString().trim())
            .matchType(ProviderMatch.MatchType.GENERAL_CARE)
            .distanceKm(distance)
            .build();
    }
    
    private List<Provider> findSpecialistsForCondition(String condition) {
        // Enhanced provider filtering preserving existing repository logic
        return providerRepository.findAll().stream()
            .filter(p -> p.getIsActive() && p.getSpecialty() != null)
            .filter(p -> {
                String spec = p.getSpecialty().toLowerCase();
                switch (condition.toLowerCase()) {
                    case "endometriosis":
                        return spec.contains("endometriosis") || spec.contains("gynecolog");
                    case "fertility":
                        return spec.contains("fertility") || spec.contains("reproductive");
                    case "pcos":
                        return spec.contains("pcos") || spec.contains("hormone") || spec.contains("gynecolog");
                    default:
                        return spec.contains("gynecolog");
                }
            })
            .collect(Collectors.toList());
    }
    
    private GynecologicalProfile getGynecologicalProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
            .map(hp -> hp.getGynecologicalProfile())
            .orElse(null);
    }
    
    private Double calculateDistance(User user, Provider provider) {
        // Simplified distance calculation - would use actual geolocation in production
        // For now, return random distance for demonstration
        return Math.random() * 50; // 0-50 km range
    }
}