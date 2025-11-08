package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gynecological_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GynecologicalProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private ClientHealthProfile healthProfile;
    
    @Column(name = "age_at_first_period")
    private Integer ageAtFirstPeriod;
    
    @Column(name = "average_cycle_length")
    private Integer averageCycleLength;
    
    @Column(name = "last_period_date")
    private LocalDate lastPeriodDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_regularity")
    private CycleRegularity cycleRegularity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_intensity")
    private FlowIntensity flowIntensity;
    
    @Column(name = "pregnancies_count")
    @Builder.Default
    private Integer pregnanciesCount = 0;
    
    @Column(name = "live_births_count")
    @Builder.Default
    private Integer liveBirthsCount = 0;
    
    @Column(name = "miscarriages_count")
    @Builder.Default
    private Integer miscarriagesCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contraception_method")
    private ContraceptionMethod contraceptionMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fertility_goal")
    private FertilityGoal fertilityGoal;
    
    @Column(name = "trying_to_conceive_months")
    private Integer tryingToConcieveMonths;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status")
    private SmokingStatus smokingStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alcohol_consumption")
    private AlcoholConsumption alcoholConsumption;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_frequency")
    private ExerciseFrequency exerciseFrequency;
    
    @Column(name = "stress_level", columnDefinition = "INTEGER CHECK (stress_level >= 1 AND stress_level <= 10)")
    private Integer stressLevel;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "gynecologicalProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<MenstruationCycle> cycles = new java.util.ArrayList<>();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum CycleRegularity {
        VERY_REGULAR, REGULAR, SOMEWHAT_IRREGULAR, VERY_IRREGULAR, UNKNOWN
    }
    
    public enum FlowIntensity {
        VERY_LIGHT, LIGHT, NORMAL, HEAVY, VERY_HEAVY
    }
    
    public enum ContraceptionMethod {
        NONE, CONDOMS, BIRTH_CONTROL_PILLS, IUD, IMPLANT, INJECTION, 
        NATURAL_FAMILY_PLANNING, STERILIZATION, OTHER
    }
    
    public enum FertilityGoal {
        TRYING_TO_CONCEIVE, PREVENTING_PREGNANCY, NOT_SEXUALLY_ACTIVE, UNCERTAIN
    }
    
    public enum SmokingStatus {
        NEVER, FORMER, CURRENT_LIGHT, CURRENT_MODERATE, CURRENT_HEAVY
    }
    
    public enum AlcoholConsumption {
        NEVER, RARELY, OCCASIONALLY, REGULARLY, FREQUENTLY
    }
    
    public enum ExerciseFrequency {
        NEVER, RARELY, WEEKLY, SEVERAL_TIMES_WEEK, DAILY
    }
}