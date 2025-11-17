package com.gynaid.backend.entity;

import com.gynaid.backend.entity.client.ClientHealthProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_scores")
public class HealthScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "score_value", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal scoreValue = BigDecimal.valueOf(50.0); // Default score

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Embedded
    @Builder.Default
    private ScoreComponents components = new ScoreComponents();

    @Column(name = "previous_score", precision = 5, scale = 2)
    private BigDecimal previousScore;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "score_version")
    @Builder.Default
    private Integer scoreVersion = 1;

    @Column(name = "trend")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScoreTrend trend = ScoreTrend.STABLE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreComponents {
        @Column(name = "profile_completion")
        @Builder.Default
        private BigDecimal profileCompletion = BigDecimal.valueOf(20.0); // 20% weight

        @Column(name = "engagement_adherence")
        @Builder.Default
        private BigDecimal engagementAdherence = BigDecimal.valueOf(25.0); // 25% weight

        @Column(name = "clinical_indicators")
        @Builder.Default
        private BigDecimal clinicalIndicators = BigDecimal.valueOf(30.0); // 30% weight

        @Column(name = "self_reported")
        @Builder.Default
        private BigDecimal selfReported = BigDecimal.valueOf(25.0); // 25% weight

        // Calculate weighted total score
        public BigDecimal getTotalScore() {
            return profileCompletion
                .add(engagementAdherence)
                .add(clinicalIndicators)
                .add(selfReported);
        }
    }

    public enum ScoreTrend {
        IMPROVING,    // Score increased from previous period
        DECLINING,    // Score decreased from previous period
        STABLE        // Score remained the same
    }

    // Add missing getter methods
    public BigDecimal getScore() {
        return scoreValue != null ? scoreValue : score;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated != null ? lastUpdated : updatedAt;
    }

    public ScoreComponents getScoreComponents() {
        return components;
    }

    // Helper methods
    public void updateScore(BigDecimal newScore) {
        this.previousScore = this.scoreValue != null ? this.scoreValue : this.score;
        this.scoreValue = newScore;
        this.score = newScore; // Also set the new score field
        this.scoreVersion++;
        this.lastUpdated = LocalDateTime.now();
        
        // Determine trend
        if (newScore.compareTo(this.previousScore) > 0) {
            this.trend = ScoreTrend.IMPROVING;
        } else if (newScore.compareTo(this.previousScore) < 0) {
            this.trend = ScoreTrend.DECLINING;
        } else {
            this.trend = ScoreTrend.STABLE;
        }
    }

    public void recalculateScore() {
        this.scoreValue = this.components.getTotalScore();
        this.score = this.components.getTotalScore();
    }
}