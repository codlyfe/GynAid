package com.gynaid.backend.service.ai;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.client.ClientHealthProfile;
import com.gynaid.backend.entity.client.GynecologicalProfile;
import com.gynaid.backend.entity.client.MenstruationCycle;
import com.gynaid.backend.repository.UserRepository;
import com.gynaid.backend.repository.client.ClientHealthProfileRepository;
import com.gynaid.backend.service.GynecologicalProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictiveAnalyticsService {
    
    private final GynecologicalProfileService gynecologicalProfileService;
    private final UserRepository userRepository;
    private final ClientHealthProfileRepository healthProfileRepository;
    
    public LocalDate predictNextPeriod(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Use existing prediction logic as foundation, enhance with AI
            List<LocalDate> existingPredictions = gynecologicalProfileService.predictNextPeriods(user.getEmail(), 1);
            LocalDate existingPrediction = existingPredictions.isEmpty() ? null : existingPredictions.get(0);
            
            // Get additional data for enhanced prediction
            GynecologicalProfile profile = getGynecologicalProfileByUserId(userId);
            if (profile == null) {
                return existingPrediction;
            }
            
            List<MenstruationCycle> recentCycles = getRecentCyclesByUserId(userId, 6);
            
            // Enhanced prediction using pattern analysis (preserves existing logic)
            if (recentCycles.size() >= 3) {
                return enhancedCyclePrediction(recentCycles, profile);
            }
            
            return existingPrediction;
        } catch (Exception e) {
            log.error("Error in predictive analytics for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    public double calculateCycleRegularityScore(Long userId) {
        try {
            List<MenstruationCycle> cycles = getRecentCyclesByUserId(userId, 6);
            if (cycles.size() < 3) {
                return 0.5; // Insufficient data
            }
            
            // Calculate variance in cycle lengths
            double avgLength = cycles.stream()
                .mapToInt(c -> c.getCycleLength() != null ? c.getCycleLength() : 28)
                .average()
                .orElse(28.0);
            
            double variance = cycles.stream()
                .mapToDouble(c -> {
                    int length = c.getCycleLength() != null ? c.getCycleLength() : 28;
                    return Math.pow(length - avgLength, 2);
                })
                .average()
                .orElse(0.0);
            
            // Convert variance to regularity score (0-1, higher is more regular)
            double standardDeviation = Math.sqrt(variance);
            return Math.max(0, 1 - (standardDeviation / 10)); // Normalize to 0-1 scale
        } catch (Exception e) {
            log.error("Error calculating regularity score for user {}: {}", userId, e.getMessage());
            return 0.5;
        }
    }
    
    public String generateFertilityInsight(Long userId) {
        try {
            GynecologicalProfile profile = getGynecologicalProfileByUserId(userId);
            if (profile == null) {
                return "Complete your profile for personalized fertility insights.";
            }
            
            LocalDate nextPeriod = predictNextPeriod(userId);
            if (nextPeriod == null) {
                return "Track a few cycles for accurate fertility predictions.";
            }
            
            // Calculate fertile window (preserves existing medical logic)
            LocalDate ovulationDate = nextPeriod.minusDays(14);
            LocalDate fertileStart = ovulationDate.minusDays(5);
            LocalDate fertileEnd = ovulationDate.plusDays(1);
            
            if (profile.getFertilityGoal() == GynecologicalProfile.FertilityGoal.TRYING_TO_CONCEIVE) {
                return String.format("Your fertile window is predicted from %s to %s. " +
                    "Peak fertility around %s. Track ovulation signs for best timing.",
                    fertileStart, fertileEnd, ovulationDate);
            } else {
                return String.format("For contraception awareness: fertile window %s to %s. " +
                    "Use additional protection during this time.", fertileStart, fertileEnd);
            }
        } catch (Exception e) {
            log.error("Error generating fertility insight for user {}: {}", userId, e.getMessage());
            return "Unable to generate fertility insights at this time.";
        }
    }
    
    private LocalDate enhancedCyclePrediction(List<MenstruationCycle> cycles, GynecologicalProfile profile) {
        // Enhanced algorithm that builds on existing logic
        if (cycles.isEmpty()) {
            return null;
        }
        
        // Get the most recent cycle
        MenstruationCycle lastCycle = cycles.get(0);
        LocalDate lastPeriodStart = lastCycle.getStartDate();
        
        // Calculate weighted average cycle length (recent cycles weighted more)
        double weightedAvgLength = 0;
        double totalWeight = 0;
        
        for (int i = 0; i < cycles.size(); i++) {
            MenstruationCycle cycle = cycles.get(i);
            int cycleLength = cycle.getCycleLength() != null ? cycle.getCycleLength() : 28;
            double weight = Math.pow(0.8, i); // Recent cycles have higher weight
            
            weightedAvgLength += cycleLength * weight;
            totalWeight += weight;
        }
        
        int predictedLength = (int) Math.round(weightedAvgLength / totalWeight);
        
        // Adjust based on regularity pattern
        double regularityScore = calculateCycleRegularityFromList(cycles);
        if (regularityScore < 0.7) {
            // For irregular cycles, add some uncertainty buffer
            predictedLength = Math.max(21, Math.min(35, predictedLength));
        }
        
        return lastPeriodStart.plusDays(predictedLength);
    }
    
    private double calculateCycleRegularityFromList(List<MenstruationCycle> cycles) {
        if (cycles.size() < 2) {
            return 0.5;
        }
        
        double avgLength = cycles.stream()
            .mapToInt(c -> c.getCycleLength() != null ? c.getCycleLength() : 28)
            .average()
            .orElse(28.0);
        
        double variance = cycles.stream()
            .mapToDouble(c -> {
                int length = c.getCycleLength() != null ? c.getCycleLength() : 28;
                return Math.pow(length - avgLength, 2);
            })
            .average()
            .orElse(0.0);
        
        double standardDeviation = Math.sqrt(variance);
        return Math.max(0, 1 - (standardDeviation / 10));
    }
    
    private GynecologicalProfile getGynecologicalProfileByUserId(Long userId) {
        ClientHealthProfile healthProfile = healthProfileRepository.findByUserId(userId)
            .orElse(null);
        return healthProfile != null ? healthProfile.getGynecologicalProfile() : null;
    }
    
    private List<MenstruationCycle> getRecentCyclesByUserId(Long userId, int limit) {
        GynecologicalProfile profile = getGynecologicalProfileByUserId(userId);
        if (profile == null || profile.getCycles() == null) {
            return List.of();
        }
        
        return profile.getCycles().stream()
            .sorted((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()))
            .limit(limit)
            .collect(Collectors.toList());
    }
}
