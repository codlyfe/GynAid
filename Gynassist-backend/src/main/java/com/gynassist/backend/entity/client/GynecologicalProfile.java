package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive gynecological and reproductive health profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gynecological_profiles")
public class GynecologicalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false, unique = true)
    private ClientHealthProfile healthProfile;

    // Menstruation information
    @Column(name = "menarche_age")
    private Integer menarcheAge; // Age at first period

    @Column(name = "cycle_length_days")
    private Integer cycleLengthDays; // Average cycle length

    @Column(name = "period_duration_days")
    private Integer periodDurationDays; // Average period duration

    @Column(name = "cycle_regularity")
    @Enumerated(EnumType.STRING)
    private CycleRegularity cycleRegularity;

    @Column(name = "flow_intensity")
    @Enumerated(EnumType.STRING)
    private FlowIntensity flowIntensity;

    @Column(name = "last_menstrual_period")
    private LocalDate lastMenstrualPeriod;

    @Column(name = "menstrual_symptoms", columnDefinition = "TEXT")
    private String menstrualSymptoms; // JSON array: ["cramps", "mood swings", "migraines"]

    // Fertility information
    @Column(name = "fertility_goal")
    @Enumerated(EnumType.STRING)
    private FertilityGoal fertilityGoal;

    @Column(name = "current_contraception")
    @Enumerated(EnumType.STRING)
    private ContraceptionMethod currentContraception;

    @Column(name = "contraception_start_date")
    private LocalDate contraceptionStartDate;

    // Pregnancy history (GPA: Gravida, Para, Abortus)
    @Column(name = "gravida")
    @Builder.Default
    private Integer gravida = 0; // Number of pregnancies

    @Column(name = "para")
    @Builder.Default
    private Integer para = 0; // Number of live births

    @Column(name = "abortus")
    @Builder.Default
    private Integer abortus = 0; // Number of abortions/miscarriages

    // Lifestyle factors
    @Column(name = "smoking_status")
    @Enumerated(EnumType.STRING)
    private SmokingStatus smokingStatus;

    @Column(name = "alcohol_consumption")
    @Enumerated(EnumType.STRING)
    private AlcoholConsumption alcoholConsumption;

    @Column(name = "exercise_frequency")
    @Enumerated(EnumType.STRING)
    private ExerciseFrequency exerciseFrequency;

    // Menstruation cycle tracking
    @OneToMany(mappedBy = "gynecologicalProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<MenstruationCycle> cycleHistory = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CycleRegularity {
        REGULAR, IRREGULAR, VERY_IRREGULAR
    }

    public enum FlowIntensity {
        LIGHT, MEDIUM, HEAVY, FLOODING
    }

    public enum FertilityGoal {
        TRYING_TO_CONCEIVE, AVOIDING_PREGNANCY, NOT_SURE, NOT_APPLICABLE
    }

    public enum ContraceptionMethod {
        NONE, PILL, IUD, IMPLANT, INJECTION, PATCH, RING, BARRIER, STERILIZATION, OTHER
    }

    public enum SmokingStatus {
        NEVER, OCCASIONAL, REGULAR, FORMER_SMOKER
    }

    public enum AlcoholConsumption {
        NONE, OCCASIONAL, REGULAR, HEAVY
    }

    public enum ExerciseFrequency {
        NONE, OCCASIONAL, WEEKLY, SEVERAL_TIMES_WEEK, DAILY
    }
}

