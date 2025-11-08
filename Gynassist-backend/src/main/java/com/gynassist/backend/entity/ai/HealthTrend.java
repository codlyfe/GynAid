package com.gynassist.backend.entity.ai;

import com.gynassist.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_trends")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthTrend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trend_type", nullable = false)
    private TrendType trendType;
    
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @Column(name = "current_value")
    private Double currentValue;
    
    @Column(name = "previous_value")
    private Double previousValue;
    
    @Column(name = "trend_direction")
    @Enumerated(EnumType.STRING)
    private TrendDirection trendDirection;
    
    @Column(name = "significance_score")
    private Double significanceScore;
    
    @Column(name = "analysis_period_days")
    private Integer analysisPeriodDays;
    
    @Column(name = "calculated_at", nullable = false)
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
    
    public enum TrendType {
        CYCLE_REGULARITY, SYMPTOM_SEVERITY, MOOD_PATTERN, 
        PAIN_LEVEL, FLOW_INTENSITY, FERTILITY_INDICATOR
    }
    
    public enum TrendDirection {
        IMPROVING, STABLE, DECLINING, FLUCTUATING
    }
}