package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private ClientHealthProfile healthProfile;
    
    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;
    
    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;
    
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;
    
    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory;
    
    @Column(name = "previous_surgeries", columnDefinition = "TEXT")
    private String previousSurgeries;
    
    @Column(name = "reproductive_health_issues", columnDefinition = "TEXT")
    private String reproductiveHealthIssues;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "hiv_status")
    private HIVStatus hivStatus;
    
    @Column(name = "last_pap_smear_date")
    private String lastPapSmearDate;
    
    @Column(name = "last_mammogram_date")
    private String lastMammogramDate;
    
    @Column(name = "vaccination_history", columnDefinition = "TEXT")
    private String vaccinationHistory;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "disclosure_preference")
    @Builder.Default
    private DisclosurePreference disclosurePreference = DisclosurePreference.PRIVATE;
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
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
    
    public enum HIVStatus {
        NEGATIVE, POSITIVE, UNKNOWN, PREFER_NOT_TO_SAY
    }
    
    public enum DisclosurePreference {
        PRIVATE, SHARE_WITH_PROVIDERS, SHARE_FOR_RESEARCH, PUBLIC
    }
}