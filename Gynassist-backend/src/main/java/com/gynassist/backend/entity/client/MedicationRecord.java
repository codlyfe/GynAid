package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Current medications and supplements for a client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medication_records")
public class MedicationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_history_id", nullable = false)
    private MedicalHistory medicalHistory;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Column(name = "dosage")
    private String dosage; // e.g., "10mg", "500mg twice daily"

    @Column(name = "frequency")
    private String frequency; // e.g., "Once daily", "Twice daily", "As needed"

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "prescribed_by")
    private String prescribedBy; // Doctor name

    @Column(name = "reason")
    private String reason; // Why taking this medication

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

