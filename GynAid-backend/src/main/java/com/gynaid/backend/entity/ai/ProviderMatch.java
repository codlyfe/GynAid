package com.gynaid.backend.entity.ai;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    
    @Column(name = "match_score", nullable = false)
    private Double matchScore;
    
    @Column(name = "match_reason", columnDefinition = "TEXT")
    private String matchReason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type")
    private MatchType matchType;
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    @Column(name = "user_viewed")
    @Builder.Default
    private Boolean userViewed = false;
    
    public enum MatchType {
        CONDITION_SPECIALIST, LOCATION_BASED, EXPERIENCE_MATCH, 
        EMERGENCY_AVAILABLE, GENERAL_CARE
    }
}
