package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Medical history for clients.
 * Stores chronic conditions, allergies, surgical history, and medications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medical_history")
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false, unique = true)
    private ClientHealthProfile healthProfile;

    // Chronic conditions (stored as JSON array for flexibility)
    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions; // JSON array: ["Hypertension", "Diabetes", etc.]

    // HIV status with enhanced privacy
    @Column(name = "hiv_status")
    @Enumerated(EnumType.STRING)
    private HIVStatus hivStatus;

    @Column(name = "hiv_status_disclosure_preference")
    @Enumerated(EnumType.STRING)
    private DisclosurePreference hivStatusDisclosurePreference;

    // Allergies (stored as JSON)
    @Column(name = "drug_allergies", columnDefinition = "TEXT")
    private String drugAllergies; // JSON array

    @Column(name = "food_allergies", columnDefinition = "TEXT")
    private String foodAllergies; // JSON array

    @Column(name = "environmental_allergies", columnDefinition = "TEXT")
    private String environmentalAllergies; // JSON array

    // Surgical history
    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<SurgicalRecord> surgicalHistory = new ArrayList<>();

    // Current medications
    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<MedicationRecord> currentMedications = new ArrayList<>();

    // Family history
    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory; // JSON: {"ovarianCancer": true, "breastCancer": false, etc.}

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum HIVStatus {
        NEGATIVE, POSITIVE, UNKNOWN, PREFER_NOT_TO_SAY
    }

    public enum DisclosurePreference {
        FULL_DISCLOSURE, PROVIDER_ONLY, EMERGENCY_ONLY, NONE
    }
}

