package com.gynaid.backend.service;

import com.gynaid.backend.entity.HealthScore;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.client.ClientHealthProfile;
import com.gynaid.backend.entity.client.GynecologicalProfile;
import com.gynaid.backend.entity.client.MedicalVitals;
import com.gynaid.backend.entity.client.MenstruationCycle;
import com.gynaid.backend.repository.HealthScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health Score Service for calculating dynamic health scores
 * Implements weighted scoring algorithm based on health data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthScoreService {

    private final HealthScoreRepository healthScoreRepository;

    // Scoring weights - configurable based on medical guidelines
    private static final Map<String, Double> SCORING_WEIGHTS = Map.of(
        "profile_completion", 0.25,      // 25% - Profile completeness
        "menstrual_health", 0.30,        // 30% - Menstrual cycle regularity
        "vital_signs", 0.20,             // 20% - Blood pressure, BMI, etc.
        "medical_history", 0.15,         // 15% - Past medical conditions
        "engagement", 0.10               // 10% - App usage and activity
    );

    /**
     * Calculate and save health score for a client
     */
    @Transactional
    public HealthScore calculateHealthScore(ClientHealthProfile healthProfile) {
        log.info("Calculating health score for client: {}", healthProfile.getUser().getEmail());
        
        try {
            ScoreCalculation scoreCalculation = calculateScoreComponents(healthProfile);
            
            // Calculate weighted total score
            double totalScore = scoreCalculation.calculateWeightedScore();
            
            // Normalize to 0-100 scale
            int normalizedScore = normalizeScore(totalScore);
            
            // Create or update health score record
            HealthScore.ScoreComponents components = new HealthScore.ScoreComponents();
            // Convert Map<String, Double> to ScoreComponents
            if (scoreCalculation.getComponents() != null) {
                Map<String, Double> compMap = scoreCalculation.getComponents();
                components.setProfileCompletion(BigDecimal.valueOf(compMap.getOrDefault("profileCompletion", 20.0)));
                components.setEngagementAdherence(BigDecimal.valueOf(compMap.getOrDefault("engagementAdherence", 25.0)));
                components.setClinicalIndicators(BigDecimal.valueOf(compMap.getOrDefault("clinicalIndicators", 30.0)));
                components.setSelfReported(BigDecimal.valueOf(compMap.getOrDefault("selfReported", 25.0)));
            }
            
            HealthScore healthScore = HealthScore.builder()
                .client(healthProfile.getUser())
                .scoreValue(BigDecimal.valueOf(normalizedScore))
                .previousScore(BigDecimal.valueOf(getPreviousScore(healthProfile.getUser()) != null ? getPreviousScore(healthProfile.getUser()) : 0))
                .components(components)
                .trend(calculateTrend(healthProfile.getUser(), (int) normalizedScore))
                .build();
            
            return healthScoreRepository.save(healthScore);
            
        } catch (Exception e) {
            log.error("Error calculating health score", e);
            throw new RuntimeException("Health score calculation failed", e);
        }
    }

    /**
     * Get health score trend data for visualization
     */
    @Transactional(readOnly = true)
    public List<HealthScore> getHealthScoreHistory(User client, int months) {
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(months);
        return healthScoreRepository.findByClientAndCreatedAtAfterOrderByCreatedAtDesc(client, fromDate);
    }

    /**
     * Get current health score for a client
     */
    @Transactional(readOnly = true)
    public HealthScore getCurrentHealthScore(User client) {
        return healthScoreRepository.findTopByClientOrderByCreatedAtDesc(client)
            .orElse(createDefaultHealthScore(client));
    }

    /**
     * Get health insights and recommendations
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHealthInsights(User client) {
        HealthScore currentScore = getCurrentHealthScore(client);
        List<HealthScore> history = getHealthScoreHistory(client, 6);
        
        Map<String, Object> insights = new HashMap<>();
        insights.put("currentScore", currentScore);
        insights.put("trend", currentScore.getTrend());
        insights.put("history", history);
        insights.put("recommendations", generateRecommendationsFromHealthScore(currentScore));
        insights.put("improvementAreas", identifyImprovementAreas(currentScore));
        insights.put("strengths", identifyStrengths(currentScore));
        
        return insights;
    }

    /**
     * Generate trend visualization data for charts
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateTrendVisualizationData(List<HealthScore> history) {
        Map<String, Object> visualizationData = new HashMap<>();
        
        if (history.isEmpty()) {
            return visualizationData;
        }
        
        // Prepare time series data
        List<Map<String, Object>> scoreData = history.stream()
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .map(score -> {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", score.getCreatedAt().toLocalDate());
                dataPoint.put("score", score.getScoreValue().doubleValue());
                dataPoint.put("trend", score.getTrend().name());
                return dataPoint;
            })
            .toList();
        
        visualizationData.put("timeSeriesData", scoreData);
        visualizationData.put("summary", calculateTrendSummary(history));
        visualizationData.put("milestones", identifyScoreMilestones(history));
        
        return visualizationData;
    }

    /**
     * Enable real-time health score updates
     */
    @Transactional
    public void enableRealTimeUpdates(User client) {
        // This would integrate with a message broker like Redis or RabbitMQ
        // For now, mark the user for real-time updates
        log.info("Real-time updates enabled for client: {}", client.getEmail());
    }

    /**
     * Check if real-time updates are enabled for a client
     */
    @Transactional(readOnly = true)
    public boolean isRealTimeUpdatesEnabled(User client) {
        // In a real implementation, this would check a user preference or subscription
        return true; // Default to enabled for now
    }

    /**
     * Get normalized scoring data for display
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNormalizedScoringData(HealthScore healthScore) {
        Map<String, Object> normalizedData = new HashMap<>();
        
        // Overall score with context
        double scoreValue = healthScore.getScoreValue().doubleValue();
        normalizedData.put("overall", Map.of(
            "score", scoreValue,
            "category", categorizeHealthScore((int) scoreValue),
            "context", getScoreContext((int) scoreValue)
        ));
        
        // Component scores with normalization
        Map<String, Double> components = new HashMap<>();
        if (healthScore.getScoreComponents() != null) {
            HealthScore.ScoreComponents comps = healthScore.getScoreComponents();
            components.put("profileCompletion", comps.getProfileCompletion().doubleValue());
            components.put("engagementAdherence", comps.getEngagementAdherence().doubleValue());
            components.put("clinicalIndicators", comps.getClinicalIndicators().doubleValue());
            components.put("selfReported", comps.getSelfReported().doubleValue());
        }
        Map<String, Object> normalizedComponents = new HashMap<>();
        
        components.forEach((component, score) -> {
            normalizedComponents.put(component, Map.of(
                "score", normalizeComponentScore(component, score),
                "weight", SCORING_WEIGHTS.get(component),
                "contribution", calculateComponentContribution(component, score),
                "status", getComponentStatus(component, score)
            ));
        });
        
        normalizedData.put("components", normalizedComponents);
        normalizedData.put("lastUpdated", healthScore.getLastUpdated());
        
        return normalizedData;
    }

    // Helper methods for trend visualization
    private Map<String, Object> calculateTrendSummary(List<HealthScore> history) {
        if (history.size() < 2) {
            return Map.of("trend", "Insufficient data", "change", 0);
        }
        
        HealthScore latest = history.get(0);
        HealthScore previous = history.get(1);
        BigDecimal change = latest.getScoreValue().subtract(previous.getScoreValue());
        
        return Map.of(
            "trend", latest.getTrend().name(),
            "change", change.intValue(),
            "percentageChange", calculatePercentageChange(previous.getScoreValue().intValue(), latest.getScoreValue().intValue())
        );
    }

    private List<String> identifyScoreMilestones(List<HealthScore> history) {
        List<String> milestones = new ArrayList<>();
        
        for (HealthScore score : history) {
            if (score.getScoreValue().compareTo(BigDecimal.valueOf(90)) >= 0) {
                milestones.add("Excellent health score achieved!");
            } else if (score.getScoreValue().compareTo(BigDecimal.valueOf(80)) >= 0) {
                milestones.add("Good health score milestone reached!");
            } else if (score.getScoreValue().compareTo(BigDecimal.valueOf(70)) >= 0) {
                milestones.add("Above average health score reached!");
            }
        }
        
        return milestones;
    }

    private String categorizeHealthScore(int score) {
        if (score >= 90) return "Excellent";
        if (score >= 80) return "Good";
        if (score >= 70) return "Above Average";
        if (score >= 60) return "Average";
        if (score >= 50) return "Below Average";
        return "Needs Improvement";
    }

    private String getScoreContext(int score) {
        return "Your health score of " + score + " falls in the " + categorizeHealthScore(score) + " range.";
    }

    private double normalizeComponentScore(String component, double rawScore) {
        // Normalize to 0-100 scale with component-specific adjustments
        return Math.max(0, Math.min(100, rawScore));
    }

    private double calculateComponentContribution(String component, double score) {
        double weight = SCORING_WEIGHTS.getOrDefault(component, 0.0);
        return (score * weight) / 100.0;
    }

    private String getComponentStatus(String component, double score) {
        if (score >= 80) return "Good";
        if (score >= 60) return "Fair";
        return "Needs Attention";
    }

    private double calculatePercentageChange(int previousScore, int currentScore) {
        if (previousScore == 0) return 0.0;
        return ((double)(currentScore - previousScore) / previousScore) * 100.0;
    }

    // Score calculation methods
    private ScoreCalculation calculateScoreComponents(ClientHealthProfile healthProfile) {
        ScoreCalculation calculation = new ScoreCalculation();
        
        // Profile completion score (25%)
        calculation.addComponent("profile_completion", calculateProfileCompletionScore(healthProfile));
        
        // Menstrual health score (30%)
        calculation.addComponent("menstrual_health", calculateMenstrualHealthScore(healthProfile));
        
        // Vital signs score (20%)
        calculation.addComponent("vital_signs", calculateVitalSignsScore(healthProfile));
        
        // Medical history score (15%)
        calculation.addComponent("medical_history", calculateMedicalHistoryScore(healthProfile));
        
        // Engagement score (10%)
        calculation.addComponent("engagement", calculateEngagementScore(healthProfile));
        
        return calculation;
    }

    private double calculateProfileCompletionScore(ClientHealthProfile healthProfile) {
        double score = 0.0;
        int totalFields = 6;
        int completedFields = 0;
        
        if (healthProfile.getUser().getFirstName() != null) completedFields++;
        if (healthProfile.getUser().getLastName() != null) completedFields++;
        if (healthProfile.getMedicalVitals() != null) completedFields++;
        if (healthProfile.getGynecologicalProfile() != null) completedFields++;
        if (healthProfile.getUser().getPhysicalAddress() != null) completedFields++;
        if (healthProfile.getUser().getDateOfBirth() != null) completedFields++;
        
        return (completedFields / (double) totalFields) * 100.0;
    }

    private double calculateMenstrualHealthScore(ClientHealthProfile healthProfile) {
        if (healthProfile.getGynecologicalProfile() == null) {
            return 0.0; // No data available
        }
        
        GynecologicalProfile gyneProfile = healthProfile.getGynecologicalProfile();
        double score = 0.0;
        
        // Regularity (40% weight)
        List<MenstruationCycle> cycles = gyneProfile.getMenstrualCycles();
        if (cycles != null && cycles.size() >= 3) {
            double regularity = calculateCycleRegularity(cycles);
            score += regularity * 0.4;
        }
        
        // Cycle length normalcy (30% weight)
        if (cycles != null && !cycles.isEmpty()) {
            double lengthScore = calculateCycleLengthScore(cycles);
            score += lengthScore * 0.3;
        }
        
        // Symptoms management (30% weight)
        double symptomsScore = calculateSymptomsScore(gyneProfile);
        score += symptomsScore * 0.3;
        
        return Math.min(score, 100.0);
    }

    private double calculateCycleRegularity(List<MenstruationCycle> cycles) {
        if (cycles.size() < 3) return 50.0; // Default for insufficient data
        
        // Calculate standard deviation of cycle lengths
        List<Integer> cycleLengths = cycles.stream()
            .map(MenstruationCycle::getCycleLength)
            .filter(length -> length != null && length > 0)
            .toList();
        
        if (cycleLengths.size() < 3) return 50.0;
        
        double mean = cycleLengths.stream().mapToInt(Integer::intValue).average().orElse(28.0);
        double variance = cycleLengths.stream()
            .mapToInt(Integer::intValue)
            .mapToDouble(length -> Math.pow(length - mean, 2))
            .average()
            .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Score based on how close cycle lengths are (lower std dev = higher score)
        return Math.max(0.0, 100.0 - (stdDev * 2));
    }

    private double calculateCycleLengthScore(List<MenstruationCycle> cycles) {
        double totalScore = 0.0;
        int validCycles = 0;
        
        for (MenstruationCycle cycle : cycles) {
            if (cycle.getCycleLength() != null) {
                int length = cycle.getCycleLength();
                if (length >= 21 && length <= 35) {
                    totalScore += 100.0; // Normal range
                } else if (length >= 18 && length <= 40) {
                    totalScore += 70.0; // Acceptable range
                } else {
                    totalScore += 40.0; // Outside normal range
                }
                validCycles++;
            }
        }
        
        return validCycles > 0 ? totalScore / validCycles : 0.0;
    }

    private double calculateSymptomsScore(GynecologicalProfile profile) {
        double score = 100.0; // Start with perfect score
        
        // Deduct points for severe symptoms
        if (profile.getDysmenorrheaSeverity() != null) {
            int severityValue = switch(profile.getDysmenorrheaSeverity()) {
                case NONE -> 0;
                case MILD -> 1;
                case MODERATE -> 2;
                case SEVERE -> 3;
            };
            score -= severityValue * 10;
        }
        
        if (profile.getPmsSymptoms() != null) {
            int symptomCount = profile.getPmsSymptoms().size();
            score -= symptomCount * 5;
        }
        
        return Math.max(0.0, score);
    }

    private double calculateVitalSignsScore(ClientHealthProfile healthProfile) {
        if (healthProfile.getMedicalVitals() == null) {
            return 0.0;
        }
        
        MedicalVitals vitals = healthProfile.getMedicalVitals();
        double score = 0.0;
        
        // BMI score (40% weight)
        score += calculateBMIScore(vitals) * 0.4;
        
        // Blood pressure score (30% weight)
        score += calculateBloodPressureScore(vitals) * 0.3;
        
        // Heart rate score (20% weight)
        score += calculateHeartRateScore(vitals) * 0.2;
        
        // Other vitals score (10% weight)
        score += calculateOtherVitalsScore(vitals) * 0.1;
        
        return Math.min(score, 100.0);
    }

    private double calculateBMIScore(MedicalVitals vitals) {
        if (vitals.getHeight() == null || vitals.getWeight() == null) {
            return 50.0; // Neutral score for missing data
        }
        
        double heightInMeters = vitals.getHeight() / 100.0;
        double bmi = vitals.getWeight() / (heightInMeters * heightInMeters);
        
        // BMI scoring based on WHO categories
        if (bmi >= 18.5 && bmi < 25.0) {
            return 100.0; // Normal weight
        } else if (bmi >= 25.0 && bmi < 30.0) {
            return 80.0; // Overweight
        } else if (bmi >= 30.0 && bmi < 35.0) {
            return 60.0; // Obese class I
        } else if (bmi >= 35.0 && bmi < 40.0) {
            return 40.0; // Obese class II
        } else {
            return 20.0; // Obese class III or underweight
        }
    }

    private double calculateBloodPressureScore(MedicalVitals vitals) {
        if (vitals.getSystolicBP() == null || vitals.getDiastolicBP() == null) {
            return 70.0; // Default for missing data
        }
        
        int systolic = vitals.getSystolicBP();
        int diastolic = vitals.getDiastolicBP();
        
        // Simplified blood pressure scoring
        if (systolic < 120 && diastolic < 80) {
            return 100.0; // Normal
        } else if (systolic < 130 && diastolic < 80) {
            return 90.0; // Elevated
        } else if ((systolic >= 130 && systolic < 140) || (diastolic >= 80 && diastolic < 90)) {
            return 70.0; // Stage 1 hypertension
        } else if (systolic >= 140 || diastolic >= 90) {
            return 50.0; // Stage 2 hypertension
        } else {
            return 30.0; // Hypertensive crisis
        }
    }

    private double calculateHeartRateScore(MedicalVitals vitals) {
        if (vitals.getHeartRate() == null) {
            return 80.0; // Default for missing data
        }
        
        int heartRate = vitals.getHeartRate();
        
        // Normal resting heart rate is 60-100 bpm
        if (heartRate >= 60 && heartRate <= 100) {
            return 100.0;
        } else if (heartRate >= 50 && heartRate < 60) {
            return 85.0; // Bradycardia (mild)
        } else if (heartRate > 100 && heartRate <= 110) {
            return 85.0; // Tachycardia (mild)
        } else if (heartRate < 50 || heartRate > 110) {
            return 60.0; // Significant deviation
        } else {
            return 30.0; // Extreme values
        }
    }

    private double calculateOtherVitalsScore(MedicalVitals vitals) {
        // For now, return a neutral score
        // In a real implementation, this would include temperature, respiratory rate, etc.
        return 80.0;
    }

    private double calculateMedicalHistoryScore(ClientHealthProfile healthProfile) {
        // Simplified scoring based on absence of major conditions
        // In reality, this would be more sophisticated based on actual medical conditions
        return 80.0; // Assume good medical history unless data indicates otherwise
    }

    private double calculateEngagementScore(ClientHealthProfile healthProfile) {
        // This would be based on app usage metrics, appointment attendance, etc.
        // For now, return a neutral score
        return 70.0;
    }

    private int normalizeScore(double rawScore) {
        // Ensure score is within 0-100 range
        return (int) Math.max(0, Math.min(100, Math.round(rawScore)));
    }

    private Integer getPreviousScore(User user) {
        return healthScoreRepository.findTopByClientOrderByCreatedAtDesc(user)
            .map(score -> score.getScoreValue().intValue())
            .orElse(null);
    }

    private HealthScore.ScoreTrend calculateTrend(User user, int currentScore) {
        Integer previousScore = getPreviousScore(user);
        
        if (previousScore == null) {
            return HealthScore.ScoreTrend.STABLE; // First score, treat as stable
        }
        
        int difference = currentScore - previousScore;
        if (difference > 5) {
            return HealthScore.ScoreTrend.IMPROVING;
        } else if (difference < -5) {
            return HealthScore.ScoreTrend.DECLINING;
        } else {
            return HealthScore.ScoreTrend.STABLE;
        }
    }

    private List<String> generateRecommendations(ScoreCalculation calculation) {
        List<String> recommendations = new ArrayList<>();
        
        // Profile completion recommendations
        if (calculation.getComponentScore("profile_completion") < 80) {
            recommendations.add("Complete your health profile for better insights");
        }
        
        // Menstrual health recommendations
        if (calculation.getComponentScore("menstrual_health") < 60) {
            recommendations.add("Track your menstrual cycle regularly for better health monitoring");
        }
        
        // Vital signs recommendations
        if (calculation.getComponentScore("vital_signs") < 70) {
            recommendations.add("Monitor your vital signs regularly and consult healthcare providers when needed");
        }
        
        return recommendations;
    }

    private List<String> generateRecommendationsFromHealthScore(HealthScore healthScore) {
        List<String> recommendations = new ArrayList<>();
        
        if (healthScore.getScoreValue().intValue() < 70) {
            recommendations.add("Focus on overall health improvement");
        }
        
        if (healthScore.getScoreComponents() != null) {
            HealthScore.ScoreComponents comps = healthScore.getScoreComponents();
            if (comps.getProfileCompletion().intValue() < 80) {
                recommendations.add("Complete your health profile for better insights");
            }
        }
        
        return recommendations;
    }

    private List<String> identifyImprovementAreas(HealthScore healthScore) {
        List<String> improvements = new ArrayList<>();
        
        if (healthScore.getScoreValue().intValue() < 70) {
            improvements.add("Overall health score needs attention");
        }
        
        // Add specific improvement areas based on score components
        return improvements;
    }

    private List<String> identifyStrengths(HealthScore healthScore) {
        List<String> strengths = new ArrayList<>();
        
        if (healthScore.getScoreValue().doubleValue() >= 80) {
            strengths.add("Excellent overall health score");
        }
        
        // Add specific strengths based on score components
        return strengths;
    }

    private HealthScore createDefaultHealthScore(User client) {
        return HealthScore.builder()
            .client(client)
            .scoreValue(BigDecimal.valueOf(50)) // Neutral starting score
            .previousScore(null)
            .components(new HealthScore.ScoreComponents())
            .trend(HealthScore.ScoreTrend.STABLE)
            .build();
    }

    // Helper class for score calculations
    private static class ScoreCalculation {
        private final Map<String, Double> components = new HashMap<>();
        private final Map<String, Object> factors = new HashMap<>();
        
        public void addComponent(String name, double score) {
            components.put(name, score);
            factors.put(name + "_raw", score);
            factors.put(name + "_weight", SCORING_WEIGHTS.getOrDefault(name, 0.0));
        }
        
        public double calculateWeightedScore() {
            return components.entrySet().stream()
                .mapToDouble(entry -> {
                    String component = entry.getKey();
                    double score = entry.getValue();
                    double weight = SCORING_WEIGHTS.getOrDefault(component, 0.0);
                    return score * weight;
                })
                .sum();
        }
        
        public double getComponentScore(String component) {
            return components.getOrDefault(component, 0.0);
        }
        
        public Map<String, Double> getComponents() {
            return new HashMap<>(components);
        }
        
        public Map<String, Object> getFactors() {
            return new HashMap<>(factors);
        }
    }
}