package com.gynaid.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Records of past surgeries for a client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "surgical_records")
public class SurgicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_history_id", nullable = false)
    private MedicalHistory medicalHistory;

    @Column(name = "surgery_type", nullable = false)
    private String surgeryType; // e.g., "C-Section", "Appendectomy", "Myomectomy"

    @Column(name = "surgery_date")
    private LocalDate surgeryDate;

    @Column(name = "hospital_clinic_name")
    private String hospitalClinicName;

    @Column(name = "surgeon_name")
    private String surgeonName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "complications")
    private String complications;
}


