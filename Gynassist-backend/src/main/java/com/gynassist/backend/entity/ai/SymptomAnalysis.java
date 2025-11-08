package com.gynassist.backend.entity.ai;

import com.gynassist.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "symptom_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "symptoms", columnDefinition = "TEXT", nullable = false)
    private String symptoms;
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;
    
    @ElementCollection
    @CollectionTable(name = "symptom_recommendations", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "recommendation")
    private List<String> recommendations;
    
    @Column(name = "analyzed_at", nullable = false)
    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();
    
    @Column(name = "requires_provider_attention")
    @Builder.Default
    private Boolean requiresProviderAttention = false;
    
    public enum RiskLevel {
        LOW, MODERATE, HIGH, EMERGENCY
    }
}