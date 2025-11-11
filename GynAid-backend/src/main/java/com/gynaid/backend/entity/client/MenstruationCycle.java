package com.gynaid.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "menstruation_cycles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenstruationCycle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gynecological_profile_id", nullable = false)
    private GynecologicalProfile gynecologicalProfile;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "cycle_length")
    private Integer cycleLength;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_intensity")
    private FlowIntensity flowIntensity;
    
    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;
    
    @Column(name = "mood_notes", columnDefinition = "TEXT")
    private String moodNotes;
    
    @Column(name = "pain_level", columnDefinition = "INTEGER CHECK (pain_level >= 0 AND pain_level <= 10)")
    private Integer painLevel;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_predicted")
    @Builder.Default
    private Boolean isPredicted = false;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum FlowIntensity {
        SPOTTING, LIGHT, NORMAL, HEAVY, VERY_HEAVY
    }
}
