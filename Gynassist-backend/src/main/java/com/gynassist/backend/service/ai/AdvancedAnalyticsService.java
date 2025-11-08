package com.gynassist.backend.service.ai;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.ai.HealthTrend;
import com.gynassist.backend.entity.client.GynecologicalProfile;
import com.gynassist.backend.entity.client.MenstruationCycle;
import com.gynassist.backend.repository.UserRepository;
import com.gynassist.backend.repository.client.ClientHealthProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedAnalyticsService {
    
    private final UserRepository userRepository;
    private final ClientHealthProfileRepository healthProfileRepository;
    
    public List<HealthTrend> analyzeHealthTrends(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        if (profile == null || profile.getCycles() == null || profile.getCycles().size() < 3) {
            return List.of(); // Need at least 3 cycles for trend analysis
        }
        
        List<HealthTrend> trends = new ArrayList<>();
        List<MenstruationCycle> recentCycles = getRecentCycles(profile, 6);
        
        // Analyze cycle regularity trend
        trends.add(analyzeCycleRegularityTrend(user, recentCycles));
        
        // Analyze pain level trends
        trends.add(analyzePainTrend(user, recentCycles));
        
        // Analyze flow intensity trends
        trends.add(analyzeFlowTrend(user, recentCycles));
        
        return trends.stream()
            .filter(trend -> trend.getSignificanceScore() > 0.3) // Filter significant trends
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> generateHealthReport(Long userId) {
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        if (profile == null) {
            return Map.of("error", "Profile not found");
        }
        
        List<MenstruationCycle> cycles = getRecentCycles(profile, 12);
        List<HealthTrend> trends = analyzeHealthTrends(userId);
        
        // Calculate key metrics enhancing existing logic
        double avgCycleLength = cycles.stream()
            .filter(c -> c.getCycleLength() != null)
            .mapToInt(MenstruationCycle::getCycleLength)
            .average()
            .orElse(28.0);
        
        double regularityScore = calculateRegularityScore(cycles);
        String healthStatus = determineHealthStatus(regularityScore, trends);
        
        return Map.of(
            "averageCycleLength", avgCycleLength,
            "regularityScore", regularityScore,
            "healthStatus", healthStatus,
            "totalCyclesTracked", cycles.size(),
            "trendsIdentified", trends.size(),
            "lastAnalysisDate", LocalDate.now().toString(),
            "recommendations", generateHealthRecommendations(profile, trends)
        );
    }
    
    public Map<String, Object> calculateRiskAssessment(Long userId) {
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        if (profile == null) {
            Map<String, Object> defaultMap = new java.util.HashMap<>();
            defaultMap.put("overallRisk", 0.0);
            return defaultMap;
        }
        
        double irregularityRisk = assessIrregularityRisk(profile);
        double lifestyleRisk = assessLifestyleRisk(profile);
        double ageRisk = assessAgeRelatedRisk(profile);
        
        double overallRisk = (irregularityRisk + lifestyleRisk + ageRisk) / 3.0;
        
        Map<String, Object> riskMap = new java.util.HashMap<>();
        riskMap.put("overallRisk", overallRisk);
        riskMap.put("irregularityRisk", irregularityRisk);
        riskMap.put("lifestyleRisk", lifestyleRisk);
        riskMap.put("ageRisk", ageRisk);
        riskMap.put("riskLevel", overallRisk > 0.7 ? "High" : overallRisk > 0.4 ? "Moderate" : "Low");
        return riskMap;
    }
    
    private HealthTrend analyzeCycleRegularityTrend(User user, List<MenstruationCycle> cycles) {
        if (cycles.size() < 3) {
            return createDefaultTrend(user, HealthTrend.TrendType.CYCLE_REGULARITY);
        }
        
        // Calculate regularity for recent vs older cycles
        List<MenstruationCycle> recent = cycles.subList(0, Math.min(3, cycles.size()));
        List<MenstruationCycle> older = cycles.size() > 3 ? 
            cycles.subList(3, Math.min(6, cycles.size())) : List.of();
        
        double recentRegularity = calculateRegularityScore(recent);
        double olderRegularity = older.isEmpty() ? recentRegularity : calculateRegularityScore(older);
        
        HealthTrend.TrendDirection direction = determineTrendDirection(recentRegularity, olderRegularity);
        double significance = Math.abs(recentRegularity - olderRegularity);
        
        return HealthTrend.builder()
            .user(user)
            .trendType(HealthTrend.TrendType.CYCLE_REGULARITY)
            .metricName("Cycle Regularity Score")
            .currentValue(recentRegularity)
            .previousValue(olderRegularity)
            .trendDirection(direction)
            .significanceScore(significance)
            .analysisPeriodDays(90)
            .build();
    }
    
    private HealthTrend analyzePainTrend(User user, List<MenstruationCycle> cycles) {
        List<Integer> painLevels = cycles.stream()
            .filter(c -> c.getPainLevel() != null)
            .map(MenstruationCycle::getPainLevel)
            .collect(Collectors.toList());
        
        if (painLevels.size() < 2) {
            return createDefaultTrend(user, HealthTrend.TrendType.PAIN_LEVEL);
        }
        
        double avgRecent = painLevels.subList(0, Math.min(3, painLevels.size()))
            .stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double avgOlder = painLevels.size() > 3 ?
            painLevels.subList(3, painLevels.size())
                .stream().mapToInt(Integer::intValue).average().orElse(avgRecent) : avgRecent;
        
        return HealthTrend.builder()
            .user(user)
            .trendType(HealthTrend.TrendType.PAIN_LEVEL)
            .metricName("Average Pain Level")
            .currentValue(avgRecent)
            .previousValue(avgOlder)
            .trendDirection(determineTrendDirection(avgRecent, avgOlder))
            .significanceScore(Math.abs(avgRecent - avgOlder) / 10.0)
            .analysisPeriodDays(90)
            .build();
    }
    
    private HealthTrend analyzeFlowTrend(User user, List<MenstruationCycle> cycles) {
        // Simplified flow analysis - would be more sophisticated in production
        return createDefaultTrend(user, HealthTrend.TrendType.FLOW_INTENSITY);
    }
    
    private double calculateRegularityScore(List<MenstruationCycle> cycles) {
        if (cycles.size() < 2) return 0.5;
        
        List<Integer> lengths = cycles.stream()
            .filter(c -> c.getCycleLength() != null)
            .map(MenstruationCycle::getCycleLength)
            .collect(Collectors.toList());
        
        if (lengths.isEmpty()) return 0.5;
        
        double avg = lengths.stream().mapToInt(Integer::intValue).average().orElse(28.0);
        double variance = lengths.stream()
            .mapToDouble(l -> Math.pow(l - avg, 2))
            .average().orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        return Math.max(0, 1 - (stdDev / 10)); // Normalize to 0-1
    }
    
    private HealthTrend.TrendDirection determineTrendDirection(double current, double previous) {
        double diff = current - previous;
        if (Math.abs(diff) < 0.1) return HealthTrend.TrendDirection.STABLE;
        return diff > 0 ? HealthTrend.TrendDirection.IMPROVING : HealthTrend.TrendDirection.DECLINING;
    }
    
    private String determineHealthStatus(double regularityScore, List<HealthTrend> trends) {
        if (regularityScore > 0.8) return "Excellent";
        if (regularityScore > 0.6) return "Good";
        if (regularityScore > 0.4) return "Fair";
        return "Needs Attention";
    }
    
    private List<String> generateHealthRecommendations(GynecologicalProfile profile, List<HealthTrend> trends) {
        List<String> recommendations = new ArrayList<>();
        
        // Base recommendations preserving existing medical logic
        recommendations.add("Continue regular cycle tracking for better insights");
        recommendations.add("Maintain healthy lifestyle with balanced nutrition");
        
        // Trend-based recommendations
        for (HealthTrend trend : trends) {
            if (trend.getTrendDirection() == HealthTrend.TrendDirection.DECLINING) {
                switch (trend.getTrendType()) {
                    case CYCLE_REGULARITY:
                        recommendations.add("Consider stress management - irregularity detected");
                        break;
                    case PAIN_LEVEL:
                        recommendations.add("Increasing pain levels - consult healthcare provider");
                        break;
                }
            }
        }
        
        return recommendations;
    }
    
    private double assessIrregularityRisk(GynecologicalProfile profile) {
        if (profile.getCycleRegularity() == null) return 0.3;
        
        switch (profile.getCycleRegularity()) {
            case VERY_IRREGULAR: return 0.8;
            case SOMEWHAT_IRREGULAR: return 0.5;
            case REGULAR: return 0.2;
            case VERY_REGULAR: return 0.1;
            default: return 0.3;
        }
    }
    
    private double assessLifestyleRisk(GynecologicalProfile profile) {
        double risk = 0.0;
        
        if (profile.getSmokingStatus() == GynecologicalProfile.SmokingStatus.CURRENT_HEAVY) risk += 0.3;
        if (profile.getStressLevel() != null && profile.getStressLevel() > 7) risk += 0.2;
        if (profile.getExerciseFrequency() == GynecologicalProfile.ExerciseFrequency.NEVER) risk += 0.1;
        
        return Math.min(1.0, risk);
    }
    
    private double assessAgeRelatedRisk(GynecologicalProfile profile) {
        // Simplified age risk - would use actual age calculation in production
        return 0.2; // Default moderate risk
    }
    
    private HealthTrend createDefaultTrend(User user, HealthTrend.TrendType type) {
        return HealthTrend.builder()
            .user(user)
            .trendType(type)
            .metricName(type.name())
            .currentValue(0.5)
            .previousValue(0.5)
            .trendDirection(HealthTrend.TrendDirection.STABLE)
            .significanceScore(0.0)
            .analysisPeriodDays(90)
            .build();
    }
    
    private GynecologicalProfile getGynecologicalProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
            .map(hp -> hp.getGynecologicalProfile())
            .orElse(null);
    }
    
    private List<MenstruationCycle> getRecentCycles(GynecologicalProfile profile, int limit) {
        if (profile.getCycles() == null) return List.of();
        
        return profile.getCycles().stream()
            .sorted((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()))
            .limit(limit)
            .collect(Collectors.toList());
    }
}