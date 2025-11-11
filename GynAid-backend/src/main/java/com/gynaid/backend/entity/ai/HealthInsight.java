package com.gynaid.backend.entity.ai;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false)
    private InsightType type;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "confidence_score")
    private Double confidence;
    
    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    @Column(name = "acknowledged")
    @Builder.Default
    private Boolean acknowledged = false;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;
    
    public enum InsightType {
        CYCLE_PREDICTION, FERTILITY_WINDOW, HEALTH_RISK, 
        LIFESTYLE_RECOMMENDATION, PROVIDER_SUGGESTION, EMERGENCY_ALERT
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
