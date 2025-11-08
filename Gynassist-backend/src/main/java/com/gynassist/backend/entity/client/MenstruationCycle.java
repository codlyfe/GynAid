package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Individual menstruation cycle records for period tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menstruation_cycles")
public class MenstruationCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gynecological_profile_id", nullable = false)
    private GynecologicalProfile gynecologicalProfile;

    @Column(name = "cycle_start_date", nullable = false)
    private LocalDate cycleStartDate;

    @Column(name = "cycle_end_date")
    private LocalDate cycleEndDate;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "flow_intensity")
    @Enumerated(EnumType.STRING)
    private FlowIntensity flowIntensity;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms; // JSON array of symptoms

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Reuse enum from GynecologicalProfile
    public enum FlowIntensity {
        LIGHT, MEDIUM, HEAVY, FLOODING
    }
}

