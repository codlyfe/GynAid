package com.gynaid.backend.entity.ai;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_interactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "audio_transcript", columnDefinition = "TEXT")
    private String audioTranscript;
    
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type")
    private InteractionType interactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "language_code")
    @Builder.Default
    private LanguageCode languageCode = LanguageCode.EN;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();
    
    public enum InteractionType {
        SYMPTOM_LOGGING, CYCLE_TRACKING, HEALTH_QUERY, 
        EMERGENCY_REQUEST, APPOINTMENT_BOOKING
    }
    
    public enum LanguageCode {
        EN, LG, SW // English, Luganda, Swahili
    }
}
